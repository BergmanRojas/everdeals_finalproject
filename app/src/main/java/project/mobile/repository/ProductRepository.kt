package project.mobile.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import project.mobile.model.Product
import project.mobile.model.Comment
import project.mobile.model.AffiliateStats
import java.util.*

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("products")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                snapshot.toObjects(Product::class.java)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getProduct(productId: String): Product? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = db.collection("products").document(productId).get().await()
                doc.toObject(Product::class.java)
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error getting product", e)
                null
            }
        }
    }

    suspend fun getAffiliateStatsForUser(userId: String): List<AffiliateStats> {
        return withContext(Dispatchers.IO) {
            try {
                val statsRef = db.collection("affiliate_stats")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val stats = mutableListOf<AffiliateStats>()
                for (doc in statsRef.documents) {
                    val stat = doc.toObject(AffiliateStats::class.java)
                    if (stat != null) {
                        // Obtener información adicional del producto
                        val product = getProduct(stat.productId)
                        if (product != null) {
                            stats.add(stat.copy(
                                productName = product.title,
                                productImage = product.imageUrl,
                                productPrice = product.price
                            ))
                        }
                    }
                }
                stats
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error getting affiliate stats", e)
                emptyList()
            }
        }
    }

    suspend fun incrementProductClicks(productId: String, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val statsRef = db.collection("affiliate_stats")
                    .whereEqualTo("productId", productId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (statsRef.documents.isEmpty()) {
                    // Crear nuevo registro de estadísticas
                    val newStats = AffiliateStats(
                        productId = productId,
                        userId = userId,
                        clicks = 1
                    )
                    db.collection("affiliate_stats").add(newStats).await()
                } else {
                    // Actualizar registro existente
                    val docRef = statsRef.documents[0].reference
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentClicks = snapshot.getLong("clicks") ?: 0
                        transaction.update(docRef, "clicks", currentClicks + 1)
                    }.await()
                }
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error incrementing clicks", e)
                throw e
            }
        }
    }

    suspend fun recordProductSale(productId: String, userId: String, saleAmount: Double) {
        withContext(Dispatchers.IO) {
            try {
                val statsRef = db.collection("affiliate_stats")
                    .whereEqualTo("productId", productId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (statsRef.documents.isEmpty()) {
                    // Crear nuevo registro de estadísticas
                    val commission = saleAmount * 0.05 // 5% de comisión
                    val newStats = AffiliateStats(
                        productId = productId,
                        userId = userId,
                        sales = 1,
                        earnings = commission
                    )
                    db.collection("affiliate_stats").add(newStats).await()
                } else {
                    // Actualizar registro existente
                    val docRef = statsRef.documents[0].reference
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentSales = snapshot.getLong("sales") ?: 0
                        val currentEarnings = snapshot.getDouble("earnings") ?: 0.0
                        val commission = saleAmount * 0.05 // 5% de comisión
                        transaction.update(docRef, 
                            mapOf(
                                "sales" to currentSales + 1,
                                "earnings" to currentEarnings + commission
                            )
                        )
                    }.await()
                }
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error recording sale", e)
                throw e
            }
        }
    }
} 