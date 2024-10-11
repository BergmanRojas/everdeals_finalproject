package com.bergman.everdeals_finalproject.controller

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bergman.everdeals_finalproject.R
import com.bergman.everdeals_finalproject.models.Product
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CustomAdapter(var context: Context, products: List<Product>) :
    ArrayAdapter<Product?>(context, -1, products) {
    var products: List<Product> = products

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowProduct: View = inflater.inflate(R.layout.activity_item, parent, false)
        val txtName = rowProduct.findViewById<TextView>(R.id.nameProduct)
        val txtPreviousPrice = rowProduct.findViewById<TextView>(R.id.txtPreviousPrice)
        val txtCurrentPrice = rowProduct.findViewById<TextView>(R.id.txtCurrentPrice)
        val txtDiscount = rowProduct.findViewById<TextView>(R.id.txtDiscount)
        val txtUser = rowProduct.findViewById<TextView>(R.id.txtUser)
        val imageView = rowProduct.findViewById<ImageView>(R.id.imgProduct)
        val likeCount = rowProduct.findViewById<TextView>(R.id.countLike)
        val dislikeCount = rowProduct.findViewById<TextView>(R.id.countDislike)
        val imgLike = rowProduct.findViewById<ImageView>(R.id.imgLike)
        val imgDislike = rowProduct.findViewById<ImageView>(R.id.imgDislike)
        val txtOffer = rowProduct.findViewById<TextView>(R.id.txtOffer)
        val txtTime = rowProduct.findViewById<TextView>(R.id.txtTime)
        val product: Product = products[position]
        val userName = AtomicReference("")

        val imgUrl = ("https://firebasestorage.googleapis.com/v0/b/proyecto-descuentos-26422.appspot.com/o/carpeta_imagenes%2F" + product.getName()).toString() + ".jpg?alt=media&token=df4db52a-1af6-476e-9011-9c80ca1a56e0"
        Glide.with(context).load(imgUrl).into(imageView)

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val userId: String = product.getUserID() // Replace with the actual user ID you are looking for

        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    userName.set(documentSnapshot.getString("user"))
                    txtUser.text = userName.get()
                } else {
                    // Document does not exist for the provided user ID
                }
            }.addOnFailureListener { e -> }

        val productsRef: CollectionReference = db.collection("products")
        val productName: String = product.getName() // Name of the product you want to search
        val query: Query = productsRef.whereEqualTo("name", productName)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot: QuerySnapshot = task.result!!
                if (snapshot.isNotEmpty()) {
                    val document: DocumentSnapshot = snapshot.documents[0]
                    val likes: Int = document.getLong("likes")?.toInt() ?: 0
                    likeCount.text = likes.toString()
                    val dislikes: Int = document.getLong("dislikes")?.toInt() ?: 0
                    dislikeCount.text = dislikes.toString()
                    val date: Timestamp = document.getTimestamp("date")!!
                    val hoursPassed = calculateTime(date)
                    txtTime.text = "$hoursPassed h"
                } else {
                    // Product does not exist
                }
            } else {
                // Error executing the query
            }
        }

        txtName.text = product.getName()
        txtDiscount.text = "(${product.getDiscount()}%)"
        txtPreviousPrice.text = "${product.getPreviousPrice()} €"
        txtCurrentPrice.text = "${product.getCurrentPrice()} €"
        txtPreviousPrice.paintFlags = txtPreviousPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        txtOffer.setOnClickListener {
            val url: String = products[position].getLink()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        imgLike.setOnClickListener {
            likeCount.text = (likeCount.text.toString().toInt() + 1).toString()
            val productName: String = product.getName()
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val productsRef: CollectionReference = db.collection("products")
            val query: Query = productsRef.whereEqualTo("name", productName).limit(1)

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot: QuerySnapshot = task.result!!
                    if (querySnapshot.isNotEmpty()) {
                        val document: DocumentSnapshot = querySnapshot.documents[0]
                        val productId: String = document.id

                        // Update the document with the found ID
                        val productRef: DocumentReference = productsRef.document(productId)

                        // Create an object with the new data you want to update
                        val updatedData: MutableMap<String, Any> = HashMap()
                        updatedData["likes"] = likeCount.text.toString().toInt()

                        // Add other fields and values you want to update
                        productRef.update(updatedData).addOnSuccessListener {
                            // Update was successful
                        }.addOnFailureListener { e ->
                            // Error during the update
                        }
                    } else {
                        // No document found with the provided name
                    }
                } else {
                    // Error executing the query
                }
            }
        }

        imgDislike.setOnClickListener {
            dislikeCount.text = (dislikeCount.text.toString().toInt() + 1).toString()
            val productName: String = product.getName()
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val productsRef: CollectionReference = db.collection("products")
            val query: Query = productsRef.whereEqualTo("name", productName).limit(1)

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot: QuerySnapshot = task.result!!
                    if (querySnapshot.isNotEmpty()) {
                        val document: DocumentSnapshot = querySnapshot.documents[0]
                        val productId: String = document.id

                        // Update the document with the found ID
                        val productRef: DocumentReference = productsRef.document(productId)

                        // Create an object with the new data you want to update
                        val updatedData: MutableMap<String, Any> = HashMap()
                        updatedData["dislikes"] = dislikeCount.text.toString().toInt()

                        // Add other fields and values you want to update
                        productRef.update(updatedData).addOnSuccessListener {
                            // Update was successful
                        }.addOnFailureListener { e ->
                            // Error during the update
                        }
                    } else {
                        // No document found with the provided name
                    }
                } else {
                    // Error executing the query
                }
            }
        }
        return rowProduct
    }

    private fun calculateTime(timestamp: Timestamp): Long {
        val currentTimestamp: Timestamp = Timestamp.now()
        val timestampDate: Date = timestamp.toDate()
        val currentTimestampDate: Date = currentTimestamp.toDate()
        val millisecondsDifference = currentTimestampDate.time - timestampDate.time
        return TimeUnit.MILLISECONDS.toHours(millisecondsDifference)
    }
}
