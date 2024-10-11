package com.bergman.everdeals_finalproject.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bergman.everdeals_finalproject.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private var txtPopular: TextView? = null
    private var txtNew: TextView? = null
    private var txtFeatured: TextView? = null
    private val CODE = 0
    private var products: ArrayList<Product> = ArrayList()
    private var listView: ListView? = null
    private var imageView: ImageView? = null
    private var linearCategories: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtFeatured = findViewById(R.id.txtFeatured)
        txtNew = findViewById(R.id.txtNew)
        txtPopular = findViewById(R.id.txtOffers)
        listView = findViewById(R.id.listView)
        txtPopular!!.visibility = View.VISIBLE
        txtNew!!.visibility = View.GONE
        txtFeatured!!.visibility = View.GONE
        imageView = findViewById(R.id.imgMenu)
        linearCategories = findViewById(R.id.linearCategories)

        imageView!!.setOnClickListener {
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        linearCategories!!.setOnClickListener {
            val intentProducts: Intent = Intent(this@MainActivity, Categories::class.java)
            startActivity(intentProducts)
        }

        listView!!.setOnItemClickListener { parent: AdapterView<*>?, view: View, position: Int, id: Long ->
            val goToProduct: Intent = Intent(this@MainActivity, ViewProduct::class.java)
            val product: Product = products[position]
            val productBundle = Bundle().apply {
                putDouble("currentPrice", product.getCurrentPrice())
                putString("link", product.getLink())
                putDouble("previousPrice", product.getPreviousPrice())
                putString("name", product.getName())
                putString("description", product.getDescription())
                putString("image", product.getProductImage())
                putString("userId", product.getUserId())
                putString("category", product.getCategory())
                putString("productId", product.getId())
            }
            goToProduct.putExtras(productBundle)
            startActivity(goToProduct)
        }
    }

    override fun onResume() {
        super.onResume()
        // Update the ListView data
        products.clear()
        updateListViewData()
    }

    private fun updateListViewData() {
        products.clear()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef: CollectionReference = db.collection("products")

        collectionRef.get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
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
            val customProductAdapter = CustomProductAdapter(this@MainActivity, products)
            listView!!.adapter = customProductAdapter
        }.addOnFailureListener { e: Exception ->
            // Handle query error
        }
    }

    private fun updateListViewFeatured() {
        products.clear()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef: CollectionReference = db.collection("products")
        collectionRef.orderBy("likes", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
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
                val customProductAdapter = CustomProductAdapter(this@MainActivity, products)
                listView!!.adapter = customProductAdapter
            }.addOnFailureListener { e -> }
    }

    private fun updateListViewNew() {
        products.clear()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef: CollectionReference = db.collection("products")
        collectionRef.orderBy("date", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
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
                val customProductAdapter = CustomProductAdapter(this@MainActivity, products)
                listView!!.adapter = customProductAdapter
            }.addOnFailureListener { e -> }
    }

    private fun updateListViewPopular() {
        products.clear()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collectionRef: CollectionReference = db.collection("products")
        collectionRef.orderBy("sales", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
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
                val customProductAdapter = CustomProductAdapter(this@MainActivity, products)
                listView!!.adapter = customProductAdapter
            }.addOnFailureListener { e -> }
    }

    fun onClickListener(view: View) {
        when (view.id) {
            R.id.layoutPopular -> {
                txtPopular!!.visibility = View.VISIBLE
                txtNew!!.visibility = View.GONE
                txtFeatured!!.visibility = View.GONE
                updateListViewPopular()
            }

            R.id.layoutNew -> {
                txtNew!!.visibility = View.VISIBLE
                txtFeatured!!.visibility = View.GONE
                txtPopular!!.visibility = View.GONE
                updateListViewNew()
            }

            R.id.layoutFeatured -> {
                txtFeatured!!.visibility = View.VISIBLE
                txtPopular!!.visibility = View.GONE
                txtNew!!.visibility = View.GONE
                listView!!.adapter = null
                updateListViewFeatured()
            }

            R.id.imgCoins -> {
                val dialogView: View = layoutInflater.inflate(R.layout.activity_dialog_user_info, null)
                // Obtain references to the dialog elements
                val imgProfile = dialogView.findViewById<ImageView>(R.id.imgProfileDialog)
                val userNameText = dialogView.findViewById<TextView>(R.id.userName)
                val userMoneyText = dialogView.findViewById<TextView>(R.id.userMoney)
                val userSalesText = dialogView.findViewById<TextView>(R.id.userSales)

                // Set up the user's name and money amount in euros
                val sharedPreferences: SharedPreferences = getSharedPreferences("Login Data", Context.MODE_PRIVATE)
                val userName: String = sharedPreferences.getString("userName", "") ?: ""
                val db: FirebaseFirestore = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("user", userName).get()
                    .addOnSuccessListener { queryDocumentSnapshots ->
                        for (documentSnapshot in queryDocumentSnapshots) {
                            val name: String = documentSnapshot.getString("user") ?: ""
                            val amountMoney: Long = documentSnapshot.getLong("money") ?: 0
                            val amountSales: Long = documentSnapshot.getLong("sales") ?: 0
                            val imgUrl = "https://firebasestorage.googleapis.com/v0/b/proyecto-descuentos-26422.appspot.com/o/carpeta_imagenes%2F$userName.jpg?alt=media&token=8aea3353-10c8-4f93-9ece-caed6ddb1a87"
                            userNameText.text = userName
                            userMoneyText.text = "Money: $amountMoney€"
                            userSalesText.text = "Sales: $amountSales"
                            Picasso.get().load(imgUrl).into(imgProfile)
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Error performing query", e)
                    }

                // Create the custom AlertDialog
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setView(dialogView)
                    .setPositiveButton("OK") { dialog, which ->
                        // Action when clicking the OK button
                    }

                // Show the custom AlertDialog
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }

            R.id.imgAdd -> showPopupMenu(view)
        }
    }

    fun showPopupMenu(view: View?) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.submenu_product, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.shareOffer -> {
                    val intent: Intent = Intent(this@MainActivity, Share::class.java)
                    startActivityForResult(intent, CODE)
                    true
                }

                R.id.logOut -> {
                    val sharedPreferences: SharedPreferences = getSharedPreferences("Login Data", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    val context: Context = applicationContext
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        context.deleteSharedPreferences("Login Data")
                    }
                    val intent2: Intent = Intent(this@MainActivity, Login::class.java)
                    finish()
                    startActivity(intent2)
                    true
                }

                R.id.action_create_alert -> {
                    showCreateAlertDialog()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showCreateAlertDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val editTextProductName: EditText = EditText(this)
        editTextProductName.hint = "Name"
        builder.setView(editTextProductName)
            .setTitle("Create Alert")
            .setMessage("Enter the product name:")
            .setPositiveButton("Create") { dialog, which ->
                // Get the product name entered by the user and create the alert
                val productName: String = editTextProductName.text.toString().trim { it <= ' ' }
                Toast.makeText(this@MainActivity, "Product alert: $productName", Toast.LENGTH_SHORT).show()
                startListeningForProducts(productName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private var db: FirebaseFirestore? = null
    private var productsRef: CollectionReference? = null
    private var listenerRegistration: ListenerRegistration? = null

    // Method to start listening for new products
    private fun startListeningForProducts(productName: String) {
        db = FirebaseFirestore.getInstance()
        productsRef = db.collection("products")

        listenerRegistration = productsRef
            .whereEqualTo("name", productName)
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                for (documentChange in queryDocumentSnapshots!!.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        // A new product with the desired name was added
                        val documentSnapshot: DocumentSnapshot = documentChange.document
                        val name: String = documentSnapshot.getString("name") ?: ""
                        showNewProductNotification(name)
                    }
                }
            }
    }

    private fun showNewProductNotification(productName: String) {
        // Unique ID for the notification
        val notificationId = 1

        // Notification channel (required for Android 8.0 and higher)
        val channelId = "products_channel"
        val channelName = "Products"

        // Intent to open the main activity when clicking the notification
        val intent: Intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // Build the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.descuentos)
            .setContentTitle("New product: $productName")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Notification manager
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if creating the notification channel is necessary (only for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel: NotificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(notificationId, builder.build())
    }
}
