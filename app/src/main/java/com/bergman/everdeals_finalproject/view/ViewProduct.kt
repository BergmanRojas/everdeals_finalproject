package com.bergman.everdeals_finalproject.view

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot

class ViewProduct : AppCompatActivity() {
    private var editComment: EditText? = null
    private var currentProductPrice: TextView? = null
    private var previousProductPrice: TextView? = null
    private var comment: TextView? = null
    private var productDescription: TextView? = null
    private var category: TextView? = null
    private var readMore: TextView? = null
    private var btnGoToStore: Button? = null
    private var btnSendComment: Button? = null
    private var imageView: ImageView? = null
    private var imgShare: ImageView? = null
    private var comments: ArrayList<Comment> = ArrayList()
    private var listChats: ListView? = null
    private val db: FirebaseFirestore? = null
    private var commentRef: CollectionReference? = null
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_product)

        val product: Bundle = intent.extras ?: return
        currentProductPrice = findViewById(R.id.viewCurrentPrice)
        previousProductPrice = findViewById(R.id.viewPreviousPrice)
        comment = findViewById(R.id.viewProductName)
        category = findViewById(R.id.txtCategoryView)
        productDescription = findViewById(R.id.viewProductDescription)
        imageView = findViewById(R.id.viewProductImage)
        readMore = findViewById(R.id.txtReadMore)
        listChats = findViewById(R.id.listChats)
        btnSendComment = findViewById(R.id.btnSendComment)
        btnGoToStore = findViewById(R.id.btnGoToStore)
        editComment = findViewById(R.id.editComment)
        imgShare = findViewById(R.id.imgShare)

        previousProductPrice!!.paintFlags = previousProductPrice!!.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        val currentPrice = product.getDouble("currentPrice").toString()
        val previousPrice = product.getDouble("previousPrice").toString()
        val link = product.getString("link")

        previousProductPrice!!.text = "$previousPrice€"
        currentProductPrice!!.text = "$currentPrice€"
        comment!!.text = product.getString("name")
        category!!.text = "Category: ${product.getString("category")}"
        productDescription!!.text = product.getString("description")

        val userId = product.getString("userId")
        val productId = product.getString("productId")
        val img = product.getString("image")

        Glide.with(this).load(img).into(imageView)
        startListeningToComments(productId)

        btnGoToStore!!.setOnClickListener {
            val url = link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        imgShare!!.setOnClickListener {
            val linkToShare = "$link\n\nShared from the Discounts app ;)"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, linkToShare)
            }
            startActivity(Intent.createChooser(shareIntent, "Share link"))
        }

        btnSendComment!!.setOnClickListener { view ->
            val message: String = editComment?.text.toString()
            FirebaseApp.initializeApp(this@ViewProduct)
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            val commentMap: MutableMap<String, Any?> = HashMap()
            commentMap["comment"] = message
            commentMap["time"] = FieldValue.serverTimestamp()
            commentMap["productId"] = productId
            commentMap["userId"] = userId

            db.collection("comments").add(commentMap)
                .addOnSuccessListener { documentReference: DocumentReference? ->
                    startListeningToComments(productId)
                    // Hide the keyboard
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    editComment?.setText("")
                }.addOnFailureListener { e: Exception ->
                    Log.w(ContentValues.TAG, "Error adding comment", e)
                }
        }

        readMore!!.setOnClickListener {
            if (readMore!!.text == "Read more") {
                productDescription!!.maxLines = Int.MAX_VALUE
                readMore!!.text = "Read less"
            } else {
                productDescription!!.maxLines = 5
                readMore!!.text = "Read more"
            }
        }
    }

    private fun startListeningToComments(productId: String?) {
        comments.clear()
        FirebaseApp.initializeApp(this@ViewProduct)
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        commentRef = db.collection("comments")

        listenerRegistration = commentRef!!.whereEqualTo("productId", productId)
            .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                for (documentChange in queryDocumentSnapshots!!.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        try {
                            val documentSnapshot = documentChange.document
                            val commentText: String = documentSnapshot.getString("comment") ?: ""
                            val time: Timestamp = documentSnapshot.getTimestamp("time") ?: Timestamp.now()
                            val productId: String = documentSnapshot.getString("productId") ?: ""
                            val userId: String = documentSnapshot.getString("userId") ?: ""
                            val comment = Comment(userId, productId, commentText, time)
                            comments.add(comment)
                        } catch (error: Exception) {
                            println("$error error")
                        }
                    }
                }

                val customCommentAdapter = CustomCommentAdapter(this@ViewProduct, comments)
                listChats!!.adapter = customCommentAdapter
            }
    }
}
