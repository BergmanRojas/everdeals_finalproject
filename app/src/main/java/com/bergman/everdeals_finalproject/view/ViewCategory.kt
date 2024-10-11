package com.bergman.everdeals_finalproject.view

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import com.bergman.everdeals_finalproject.controller.CustomProductAdapter
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ViewCategory : AppCompatActivity() {
    private var listProducts: ListView? = null
    private var categoryName: TextView? = null
    private var products: ArrayList<Product> = ArrayList()
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_category)

        listProducts = findViewById<ListView>(R.id.listViewCategories)
        categoryName = findViewById<TextView>(R.id.categoryName)
        products.clear()

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val intent: Intent = intent
        if (intent != null) {
            category = intent.getStringExtra("category")
            categoryName!!.text = category

            val collectionRef: CollectionReference = db.collection("products")
            val query: Query = collectionRef.whereEqualTo("category", category)

            query.get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                // Here you can retrieve the documents and process the data
                for (documentSnapshot in queryDocumentSnapshots) {
                    // Access the fields of each document
                    val description: String = documentSnapshot.getString("description") ?: ""
                    val name: String = documentSnapshot.getString("name") ?: ""
                    val link: String = documentSnapshot.getString("link") ?: ""
                    val image: String = documentSnapshot.getString("image") ?: ""
                    val previousPrice: Double = documentSnapshot.getDouble("previousPrice") ?: 0.0
                    val currentPrice: Double = documentSnapshot.getDouble("currentPrice") ?: 0.0
                    val discount: Double = documentSnapshot.getDouble("discount") ?: 0.0
                    val userId: String = documentSnapshot.getString("userId") ?: ""
                    val longLikes: Long = documentSnapshot.getLong("likes") ?: 0
                    val longSales: Long = documentSnapshot.getLong("sales") ?: 0
                    val category: String = documentSnapshot.getString("category") ?: ""
                    val likes = longLikes.toInt()
                    val longDislikes: Long = documentSnapshot.getLong("dislikes") ?: 0
                    val sales = longSales.toInt()
                    val dislikes = longDislikes.toInt()

                    val product = Product(
                        name,
                        link,
                        description,
                        image,
                        userId,
                        category,
                        documentSnapshot.id,
                        previousPrice,
                        currentPrice,
                        discount,
                        likes,
                        dislikes,
                        sales
                    )
                    products.add(product)
                }

                val customProductAdapter = CustomProductAdapter(this@ViewCategory, products)
                listProducts!!.adapter = customProductAdapter
            }.addOnFailureListener { e: Exception ->
                // Handle query error
            }
        }
    }
}
