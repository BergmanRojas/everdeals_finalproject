package project.mobile.controller

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import project.mobile.models.Product

class DealRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = db.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(documentId = doc.id)
            }
            Log.d("DealRepository", "Productos cargados: ${products.size}")
            products
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al cargar productos: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addProduct(product: Product) {
        try {
            val docRef = if (product.documentId.isEmpty()) {
                db.collection("products").document()
            } else {
                db.collection("products").document(product.documentId)
            }
            val productMap = mapOf(
                "name" to product.name,
                "category" to product.category,
                "currentPrice" to product.currentPrice,
                "originalPrice" to product.originalPrice,
                "likes" to product.likes,
                "likedBy" to product.likedBy,
                "dislikes" to product.dislikes,
                "dislikedBy" to product.dislikedBy,
                "createdAt" to product.createdAt,
                "description" to product.description,
                "imageUrl" to product.imageUrl,
                "amazonUrl" to product.amazonUrl,
                "endDate" to product.endDate,
                "startDate" to product.startDate,
                "comments" to product.comments,
                "isOnline" to product.isOnline,
                "userId" to product.userId,
                "userName" to product.userName,
                "userPhotoUrl" to product.userPhotoUrl
            )
            docRef.set(productMap).await()
            Log.d("DealRepository", "Producto agregado con ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al agregar producto: ${e.message}", e)
            throw e
        }
    }

    suspend fun getProductsByVotes(): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .orderBy("likes", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al cargar por votos: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getProductsByRising(): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .orderBy("likes", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al cargar por rising: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getProductsByDate(): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al cargar por fecha: ${e.message}", e)
            getProducts()
        }
    }

    suspend fun searchProducts(query: String): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("DealRepository", "Error al buscar productos: ${e.message}", e)
            emptyList()
        }
    }
}