package project.mobile.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import project.mobile.models.Product

class DealRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    fun getProducts(): Flow<List<Product>> = flow {
        try {
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            emit(snapshot.toObjects(Product::class.java))
        } catch (e: Exception) {
            emit(emptyList()) // Enviar lista vacía en caso de error
        }
    }

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLikeDislike(productId: String, userId: String, isLike: Boolean): Result<Unit> {
        return try {
            val productRef = productsCollection.document(productId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                val product = snapshot.toObject(Product::class.java)
                    ?: throw Exception("Product not found")

                val likedBy = product.likedBy.toMutableList()
                val dislikedBy = product.dislikedBy.toMutableList()

                when {
                    isLike && userId in likedBy -> likedBy.remove(userId)
                    isLike && userId in dislikedBy -> {
                        dislikedBy.remove(userId)
                        likedBy.add(userId)
                    }
                    isLike -> likedBy.add(userId)
                    !isLike && userId in dislikedBy -> dislikedBy.remove(userId)
                    !isLike && userId in likedBy -> {
                        likedBy.remove(userId)
                        dislikedBy.add(userId)
                    }
                    else -> dislikedBy.add(userId)
                }

                transaction.update(productRef, mapOf("likedBy" to likedBy, "dislikedBy" to dislikedBy))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

