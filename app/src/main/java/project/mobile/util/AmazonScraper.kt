package project.mobile.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class AmazonScraper {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    suspend fun scrapeProductImages(url: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: throw Exception("Failed to load product page")
            val doc: Document = Jsoup.parse(html)

            val imageUrls = mutableListOf<String>()

            // Try different selectors for main image
            doc.select("#landingImage, #imgBlkFront").firstOrNull()?.let { img ->
                img.attr("data-old-hires").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
                img.attr("src").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
            }

            // Try to get additional images
            doc.select("li.image.item img, #altImages img").forEach { img ->
                img.attr("data-old-hires").takeIf { it.isNotEmpty() }?.let { imageUrls.add(it) }
                img.attr("src").takeIf { it.isNotEmpty() && !it.contains("sprite") }?.let { imageUrls.add(it) }
            }

            // Filter out duplicate URLs and small images
            imageUrls.distinct().filter { url ->
                !url.contains("sprite") && !url.contains("thumb") && url.contains("images/I")
            }
        } catch (e: Exception) {
            throw Exception("Failed to load product images: ${e.message}")
        }
    }
}

