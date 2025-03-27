package project.mobile.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.util.Log
import com.google.firebase.firestore.FieldValue

class ProductRepository(private val authRepository: AuthRepository) {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")
    private val commentsCollection = firestore.collection("comments")
    private val affiliateStatsCollection = firestore.collection("affiliate_stats")
    private val transactionsCollection = firestore.collection("transactions")
    private val userBalanceCollection = firestore.collection("user_balance")
    private val withdrawalsCollection = firestore.collection("withdrawals")
    private val alertsCollection = firestore.collection("alerts")

    suspend fun getProducts(): List<Product> {
        return try {
            Log.d("ProductRepository", "Iniciando carga de productos")
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            Log.d("ProductRepository", "Documentos encontrados: ${snapshot.documents.size}")
            
            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    val product = doc.toObject(Product::class.java)
                    if (product == null) {
                        Log.e("ProductRepository", "No se pudo convertir el documento ${doc.id} a Product")
                    } else {
                        Log.d("ProductRepository", "Producto cargado: ${product.name}")
                    }
                    product
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error al convertir documento ${doc.id}: ${e.message}")
                    null
                }
            }
            
            Log.d("ProductRepository", "Total de productos cargados: ${products.size}")
            products
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al cargar productos: ${e.message}")
            emptyList()
        }
    }

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            Log.d("ProductRepository", "Attempting to add product: ${product.name}")
            val currentUser = authRepository.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))

            val updatedProduct = product.copy(
                userId = currentUser.id,
                userName = currentUser.username,
                userPhotoUrl = currentUser.photoUrl ?: "",
                createdAt = Timestamp.now()
            )

            Log.d("ProductRepository", "Saving product to Firestore: ${updatedProduct.id}")
            productsCollection.document(updatedProduct.id).set(updatedProduct).await()
            Log.d("ProductRepository", "Product saved successfully")

            // Check for matching alerts
            Log.d("ProductRepository", "Checking for matching alerts")
            val allAlerts = alertsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()

            Log.d("ProductRepository", "Found ${allAlerts.size()} total active alerts")

            val matchingAlerts = allAlerts.documents.mapNotNull { doc ->
                try {
                    val alert = Alert(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        keyword = doc.getString("keyword") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )

                    val keyword = alert.keyword.lowercase().trim()
                    val productName = updatedProduct.name.lowercase()
                    val productDescription = updatedProduct.description.lowercase()

                    if (productName.contains(keyword) || productDescription.contains(keyword)) {
                        Log.d("ProductRepository", "Found matching alert - Keyword: $keyword, ProductName: $productName")
                        alert
                    } else null
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error processing alert document: ${e.message}")
                    null
                }
            }

            Log.d("ProductRepository", "Found ${matchingAlerts.size} matching alerts")

            // Update matching products for each alert
            matchingAlerts.forEach { alert ->
                try {
                    val matchingProductRef = firestore.collection("user_matching_products")
                        .document("${alert.userId}_${updatedProduct.id}")

                    val matchingProductData = hashMapOf(
                        "alertId" to alert.id,
                        "productId" to updatedProduct.id,
                        "userId" to alert.userId,
                        "createdAt" to Timestamp.now(),
                        "isRead" to false,
                        "keyword" to alert.keyword
                    )

                    matchingProductRef.set(matchingProductData).await()
                    Log.d("ProductRepository", "Successfully added matching product for alert ${alert.id} and user ${alert.userId}")
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error adding matching product for alert ${alert.id}", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error adding product: ${e.message}")
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

    suspend fun getProductById(productId: String): Product? {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            snapshot.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCommentsForProduct(productId: String): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("productId", productId)
                .get()
                .await()
            
            val comments = snapshot.toObjects(Comment::class.java)
            comments.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting comments for product $productId: ${e.message}")
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            Log.d("ProductRepository", "Adding comment: $comment")
            commentsCollection.document(comment.id).set(comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error adding comment: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAffiliateStats(userId: String): List<AffiliateStats> {
        return try {
            val products = productsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.id }

            val statsSnapshot = affiliateStatsCollection
                .whereIn("productId", products)
                .get()
                .await()

            statsSnapshot.toObjects(AffiliateStats::class.java)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting affiliate stats: ${e.message}")
            emptyList()
        }
    }

    suspend fun incrementProductClicks(productId: String, userId: String) {
        try {
            Log.d("ProductRepository", "Incrementando clicks para producto $productId y usuario $userId")
            
            // Primero obtener el producto para tener la información necesaria
            val product = getProductById(productId)
            if (product == null) {
                Log.e("ProductRepository", "Producto no encontrado: $productId")
                return
            }

            // Buscar o crear el documento de estadísticas
            val statsQuery = affiliateStatsCollection
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", product.userId) // Usamos el userId del creador del producto
                .get()
                .await()

            if (statsQuery.documents.isEmpty()) {
                // Crear nuevo documento de estadísticas
                val newStats = hashMapOf(
                    "productId" to productId,
                    "userId" to product.userId,
                    "clicks" to 1,
                    "sales" to 0,
                    "earnings" to 0.0,
                    "productName" to product.name,
                    "productImage" to product.imageUrl,
                    "productPrice" to product.currentPrice,
                    "lastUpdated" to Timestamp.now()
                )
                Log.d("ProductRepository", "Creando nuevo registro de estadísticas para producto $productId")
                affiliateStatsCollection.add(newStats).await()
            } else {
                // Incrementar clicks en documento existente
                val statsDoc = statsQuery.documents.first()
                Log.d("ProductRepository", "Actualizando clicks para estadísticas existentes: ${statsDoc.id}")
                statsDoc.reference.update(
                    mapOf(
                        "clicks" to FieldValue.increment(1),
                        "lastUpdated" to Timestamp.now()
                    )
                ).await()
            }
            Log.d("ProductRepository", "Click registrado exitosamente")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al registrar click", e)
            throw e
        }
    }

    suspend fun getAffiliateStatsForUser(userId: String): List<AffiliateStats> {
        return try {
            Log.d("ProductRepository", "Obteniendo estadísticas para usuario: $userId")
            
            // Obtener estadísticas donde el usuario es el creador del producto
            val statsQuery = affiliateStatsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val stats = statsQuery.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        val sales = (data["sales"] as? Long)?.toInt() ?: 0
                        val productPrice = data["productPrice"] as? Double ?: 0.0
                        val earnings = sales * productPrice * 0.02 // 2% de comisión

                        AffiliateStats(
                            productId = data["productId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            clicks = (data["clicks"] as? Long)?.toInt() ?: 0,
                            sales = sales,
                            earnings = earnings,
                            productName = data["productName"] as? String ?: "",
                            productImage = data["productImage"] as? String ?: "",
                            productPrice = productPrice
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error al convertir documento: ${e.message}")
                    null
                }
            }

            Log.d("ProductRepository", "Estadísticas obtenidas: ${stats.size}")
            stats
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al obtener estadísticas", e)
            emptyList()
        }
    }

    suspend fun updateProductSales(productId: String, userId: String, newSales: Int) {
        try {
            val product = getProductById(productId) ?: return
            val statsRef = affiliateStatsCollection
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .firstOrNull()?.reference ?: return

            val previousStats = statsRef.get().await()
            val previousSales = previousStats.getLong("sales")?.toInt() ?: 0
            val salesDifference = newSales - previousSales
            
            if (salesDifference > 0) {
                // Calcular las nuevas ganancias (2% de comisión)
                val newEarnings = salesDifference * product.currentPrice * 0.02
                
                // Actualizar el balance del usuario
                updateUserBalance(userId, newEarnings)
            }

            // Actualizar las estadísticas del producto
            statsRef.update(
                mapOf(
                    "sales" to newSales,
                    "lastUpdated" to Timestamp.now()
                )
            ).await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating sales and balance", e)
            throw e
        }
    }

    suspend fun recordProductSale(productId: String, userId: String, saleAmount: Double) {
        try {
            val statsRef = affiliateStatsCollection
                .whereEqualTo("productId", productId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (statsRef.documents.isEmpty()) {
                // Crear nuevo registro de estadísticas
                val commission = saleAmount * 0.02 // 2% de comisión
                val newStats = AffiliateStats(
                    productId = productId,
                    userId = userId,
                    sales = 1,
                    earnings = commission
                )
                affiliateStatsCollection.add(newStats).await()
            } else {
                // Actualizar registro existente
                val docRef = statsRef.documents[0].reference
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    val currentSales = snapshot.getLong("sales") ?: 0
                    val currentEarnings = snapshot.getDouble("earnings") ?: 0.0
                    val commission = saleAmount * 0.02 // 2% de comisión
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

    suspend fun getTransactions(userId: String): List<Transaction> {
        return try {
            Log.d("ProductRepository", "Obteniendo transacciones para usuario: $userId")
            
            val transactionDocs = transactionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents

            Log.d("ProductRepository", "Documentos de transacciones encontrados: ${transactionDocs.size}")

            val transactions = transactionDocs.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data != null) {
                        Transaction(
                            id = data["id"] as? String ?: doc.id,
                            userId = data["userId"] as? String ?: "",
                            amount = data["amount"] as? Double ?: 0.0,
                            status = try {
                                TransactionStatus.valueOf(data["status"] as? String ?: "PENDING")
                            } catch (e: Exception) {
                                TransactionStatus.PENDING
                            },
                            paypalEmail = data["paypalEmail"] as? String ?: "",
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                            updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error convirtiendo documento a Transaction: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }

            Log.d("ProductRepository", "Transacciones procesadas: ${transactions.size}")
            transactions
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo transacciones", e)
            emptyList()
        }
    }

    suspend fun getWithdrawals(userId: String): List<Map<String, Any>> {
        return try {
            Log.d("ProductRepository", "Obteniendo retiros para usuario: $userId")
            
            // Obtener los retiros sin ordenar
            val withdrawalDocs = withdrawalsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents

            Log.d("ProductRepository", "Documentos de retiros encontrados: ${withdrawalDocs.size}")

            // Ordenar en memoria y mapear los documentos
            withdrawalDocs
                .mapNotNull { doc -> 
                    doc.data?.plus("id" to doc.id) 
                }
                .sortedByDescending { 
                    (it["createdAt"] as? Timestamp)?.toDate() 
                }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting withdrawals", e)
            emptyList()
        }
    }

    suspend fun createWithdrawalRequest(userId: String, amount: Double, paypalEmail: String): Result<Unit> {
        return try {
            // Verificar el balance actual del usuario
            val stats = getAffiliateStatsForUser(userId)
            val totalEarnings = stats.sumOf { it.earnings }
            
            // Obtener el total de retiros completados
            val completedWithdrawals = transactionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "WITHDRAWAL")
                .whereEqualTo("status", TransactionStatus.COMPLETED.name)
                .get()
                .await()
                .documents
                .sumOf { it.getDouble("amount") ?: 0.0 }

            val availableBalance = totalEarnings - completedWithdrawals

            if (amount > availableBalance) {
                return Result.failure(Exception("Balance insuficiente"))
            }

            // Crear la transacción con el ID único
            val transactionId = UUID.randomUUID().toString()
            val timestamp = Timestamp.now()
            
            val transaction = hashMapOf(
                "id" to transactionId,
                "userId" to userId,
                "amount" to amount,
                "status" to TransactionStatus.PENDING.name,
                "paypalEmail" to paypalEmail,
                "createdAt" to timestamp,
                "updatedAt" to timestamp,
                "type" to "WITHDRAWAL"
            )

            // Ejecutar la transacción en Firestore
            firestore.runTransaction { firestoreTransaction ->
                // Guardar la transacción
                transactionsCollection.document(transactionId).set(transaction)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error creating withdrawal request", e)
            Result.failure(e)
        }
    }

    suspend fun updateWithdrawalStatus(withdrawalId: String, status: String) {
        try {
            withdrawalsCollection.document(withdrawalId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating withdrawal status", e)
            throw e
        }
    }

    suspend fun getUserBalance(userId: String): Double {
        return try {
            val balanceDoc = userBalanceCollection.document(userId).get().await()
            balanceDoc.getDouble("balance") ?: 0.0
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting user balance", e)
            0.0
        }
    }

    suspend fun updateUserBalance(userId: String, earnings: Double) {
        try {
            val balanceRef = userBalanceCollection.document(userId)
            val balanceDoc = balanceRef.get().await()

            if (!balanceDoc.exists()) {
                // Si no existe el documento, crearlo
                val initialBalance = hashMapOf(
                    "userId" to userId,
                    "balance" to earnings,
                    "lastUpdated" to Timestamp.now()
                )
                balanceRef.set(initialBalance).await()
            } else {
                // Si existe, actualizar el balance
                balanceRef.update(
                    "balance", FieldValue.increment(earnings),
                    "lastUpdated", Timestamp.now()
                ).await()
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error updating user balance", e)
            throw e
        }
    }

    suspend fun getAlerts(userId: String): List<Alert> {
        return try {
            Log.d("ProductRepository", "Getting alerts for user: $userId")
            
            val alertDocs = alertsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .documents

            Log.d("ProductRepository", "Found ${alertDocs.size} alert documents")

            alertDocs.mapNotNull { doc ->
                try {
                    Alert(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        keyword = doc.getString("keyword") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error converting document to Alert: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting alerts", e)
            emptyList()
        }
    }

    suspend fun createAlert(alert: Alert): Result<Unit> {
        return try {
            Log.d("ProductRepository", "Creating alert: ${alert.keyword}")
            
            val alertData = hashMapOf(
                "userId" to alert.userId,
                "keyword" to alert.keyword,
                "isActive" to alert.isActive,
                "createdAt" to alert.createdAt
            )

            alertsCollection.document(alert.id).set(alertData).await()
            Log.d("ProductRepository", "Alert created successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error creating alert", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAlert(alertId: String): Result<Unit> {
        return try {
            Log.d("ProductRepository", "Deleting alert: $alertId")
            alertsCollection.document(alertId).delete().await()
            Log.d("ProductRepository", "Alert deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error deleting alert", e)
            Result.failure(e)
        }
    }

    suspend fun getMatchingProducts(userId: String): List<Product> {
        return try {
            Log.d("ProductRepository", "Getting matching products for user: $userId")
            
            // Get all matching product IDs for the user
            val matchingProductsSnapshot = firestore.collection("user_matching_products")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d("ProductRepository", "Found ${matchingProductsSnapshot.size()} matching product records")

            val productIds = matchingProductsSnapshot.documents.mapNotNull { doc ->
                doc.getString("productId")?.also { productId ->
                    Log.d("ProductRepository", "Found matching product ID: $productId for keyword: ${doc.getString("keyword")}")
                }
            }

            if (productIds.isEmpty()) {
                Log.d("ProductRepository", "No matching product IDs found")
                return emptyList()
            }

            Log.d("ProductRepository", "Fetching ${productIds.size} products from products collection")

            // Get the actual products
            val products = productIds.mapNotNull { productId ->
                try {
                    val productDoc = productsCollection.document(productId).get().await()
                    val product = productDoc.toObject(Product::class.java)
                    if (product != null) {
                        Log.d("ProductRepository", "Successfully retrieved product: ${product.name}")
                    } else {
                        Log.e("ProductRepository", "Failed to convert document to Product: $productId")
                    }
                    product
                } catch (e: Exception) {
                    Log.e("ProductRepository", "Error fetching product $productId: ${e.message}")
                    null
                }
            }

            Log.d("ProductRepository", "Successfully retrieved ${products.size} matching products")
            products.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error getting matching products", e)
            emptyList()
        }
    }
}

