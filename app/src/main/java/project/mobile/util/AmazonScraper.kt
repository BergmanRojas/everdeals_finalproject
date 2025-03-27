package project.mobile.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

data class ProductScrapedData(
    val imageUrls: List<String>,
    val category: String
)

class AmazonScraper {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0"
    )

    suspend fun scrapeProductData(url: String): ProductScrapedData = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        for (userAgent in userAgents) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Cache-Control", "max-age=0")
                    .build()

                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw Exception("HTTP error: ${response.code}")
                }

                val html = response.body?.string() ?: throw Exception("Empty response body")
                val doc: Document = Jsoup.parse(html)

                val imageUrls = scrapeImages(doc)
                if (imageUrls.isEmpty()) {
                    throw Exception("No images found")
                }

                val category = scrapeCategory(doc)
                return@withContext ProductScrapedData(imageUrls, category)
            } catch (e: Exception) {
                lastException = e
                continue
            }
        }

        throw lastException ?: Exception("Failed to scrape product data")
    }

    private fun scrapeImages(doc: Document): List<String> {
        val imageUrls = mutableListOf<String>()

        // Try different selectors for main image
        doc.select("#landingImage, #imgBlkFront, #main-image, .a-dynamic-image").firstOrNull()?.let { img ->
            img.attr("data-old-hires").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
            img.attr("src").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
            img.attr("data-a-dynamic-image").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
        }

        // Try to get additional images
        doc.select("li.image.item img, #altImages img, .imgTagWrapper img").forEach { img ->
            img.attr("data-old-hires").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
            img.attr("src").takeIf { it.isNotEmpty() && !it.contains("sprite") }?.let { imageUrls.add(it) }
            img.attr("data-a-dynamic-image").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
        }

        // Filter out duplicate URLs and small images
        return imageUrls.distinct().filter { url ->
            !url.contains("sprite") && !url.contains("thumb") && 
            (url.contains("images/I") || url.contains("images/P"))
        }
    }

    private fun scrapeCategory(doc: Document): String {
        // Try multiple selectors for category
        return doc.select("""
            #wayfinding-breadcrumbs_container a,
            .a-link-normal.a-color-tertiary,
            #nav-subnav .nav-a-content,
            #searchDropdownBox option[selected],
            #nav-search-label-id
        """.trimIndent()).firstOrNull()?.text() ?: "General"
    }
}
