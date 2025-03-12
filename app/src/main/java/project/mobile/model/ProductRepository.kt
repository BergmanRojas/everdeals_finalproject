package project.mobile.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(private val authRepository: AuthRepository) {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))

            val updatedProduct = product.copy(
                userId = currentUser.id,
                userName = currentUser.username,
                userPhotoUrl = currentUser.photoUrl ?: ""
            )

            productsCollection.document(updatedProduct.id).set(updatedProduct).await()
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
                    isLike && userId in likedBy -> {
                        likedBy.remove(userId)
                        transaction.update(productRef, mapOf(
                            "likes" to product.likes - 1,
                            "likedBy" to likedBy
                        ))
                    }
                    isLike && userId in dislikedBy -> {
                        dislikedBy.remove(userId)
                        likedBy.add(userId)
                        transaction.update(productRef, mapOf(
                            "likes" to product.likes + 1,
                            "dislikes" to product.dislikes - 1,
                            "likedBy" to likedBy,
                            "dislikedBy" to dislikedBy
                        ))
                    }
                    isLike -> {
                        likedBy.add(userId)
                        transaction.update(productRef, mapOf(
                            "likes" to product.likes + 1,
                            "likedBy" to likedBy
                        ))
                    }
                    !isLike && userId in dislikedBy -> {
                        dislikedBy.remove(userId)
                        transaction.update(productRef, mapOf(
                            "dislikes" to product.dislikes - 1,
                            "dislikedBy" to dislikedBy
                        ))
                    }
                    !isLike && userId in likedBy -> {
                        likedBy.remove(userId)
                        dislikedBy.add(userId)
                        transaction.update(productRef, mapOf(
                            "likes" to product.likes - 1,
                            "dislikes" to product.dislikes + 1,
                            "likedBy" to likedBy,
                            "dislikedBy" to dislikedBy
                        ))
                    }
                    else -> {
                        dislikedBy.add(userId)
                        transaction.update(productRef, mapOf(
                            "dislikes" to product.dislikes + 1,
                            "dislikedBy" to dislikedBy
                        ))
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductsSortedBy(sortBy: String): List<Product> {
        return try {
            val query = when (sortBy) {
                "votes" -> productsCollection.orderBy("likes", Query.Direction.DESCENDING)
                "date" -> productsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
                else -> productsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
            }

            val snapshot = query.get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

