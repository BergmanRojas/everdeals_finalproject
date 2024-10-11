package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.HashSet

class Categories : AppCompatActivity() {
    private var listCategories: ListView? = null
    private var categories: ArrayList<Category> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        listCategories = findViewById(R.id.listCategories)
        categories.clear()

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef: CollectionReference = db.collection("products")

        collectionRef.get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
            val categoryNames = HashSet<String>()
            // Here you can retrieve the documents and process the data
            for (documentSnapshot in queryDocumentSnapshots) {
                // Access the fields of each document
                val categoryName: String? = documentSnapshot.getString("Categories")
                val categoryUrl: String? = documentSnapshot.getString("image")
                if (categoryName != null && !categoryNames.contains(categoryName)) {
                    val category = Category(categoryUrl ?: "", categoryName)
                    categories.add(category)
                    categoryNames.add(categoryName)
                }
            }

            for (i in categories.indices.reversed()) {
                if (categories[i] == null) {
                    categories.removeAt(i)
                }
            }

            val customCategoryAdapter = CustomCategoryAdapter(this@Categories, categories)
            listCategories!!.adapter = customCategoryAdapter
        }.addOnFailureListener { e: Exception ->
            // Handle query error
        }

        listCategories!!.setOnItemClickListener { adapterView: AdapterView<*>, view: android.view.View, i: Int, l: Long ->
            println(adapterView.selectedView)
        }
    }
}
