package com.bergman.everdeals_finalproject.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bergman.everdeals_finalproject.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.squareup.picasso.Picasso

class Share : AppCompatActivity() {
    var imgProduct: ImageView? = null
    var imgFolder: ImageView? = null
    var imgOk: ImageView? = null
    var btnUpload: Button? = null
    var btnNextOffer: Button? = null
    var btnNextCoupon: Button? = null
    var editLink: EditText? = null
    var editName: EditText? = null
    var editDescription: EditText? = null
    var editPreviousPrice: EditText? = null
    var editCurrentPrice: EditText? = null
    var editCouponLink: EditText? = null
    var editCouponCode: EditText? = null
    var editCouponName: EditText? = null
    var editDiscountCode: EditText? = null
    var editCouponDescription: EditText? = null
    var txtLink: TextView? = null
    var txtOffersLine: TextView? = null
    var txtCouponsLine: TextView? = null
    var currentPrice: Double? = null
    var previousPrice: Double? = null
    var discount: Double? = null
    var link: String? = null
    var name: String? = null
    var description: String? = null
    var userId: String? = null
    var categoryText: String? = null
    var imgText: String? = null
    var discountCode: String? = null
    var couponLink: String? = null
    var uri: Uri? = null
    var linearLink: LinearLayout? = null
    var linearOffer: LinearLayout? = null
    var linearCouponButton: LinearLayout? = null
    var linearMenu: LinearLayout? = null
    var linearCoupon: LinearLayout? = null
    var layoutOffers: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        imgProduct = findViewById(R.id.imgProductShare)
        editLink = findViewById(R.id.editLink)
        btnNextOffer = findViewById(R.id.btnNext)
        editName = findViewById(R.id.editProductName)
        layoutOffers = findViewById(R.id.layoutOffers)
        linearLink = findViewById(R.id.linearLink)
        linearCoupon = findViewById(R.id.linearCoupons)
        linearCouponButton = findViewById(R.id.layoutCoupons)
        editDescription = findViewById(R.id.editDescription)
        btnNextCoupon = findViewById(R.id.btnNextCoupon)
        editCouponLink = findViewById(R.id.editCouponLink)
        editCouponCode = findViewById(R.id.editDiscountCode)
        editPreviousPrice = findViewById(R.id.editPreviousPrice)
        editCurrentPrice = findViewById(R.id.editCurrentPrice)
        editCouponName = findViewById(R.id.editCouponName)
        editDiscountCode = findViewById(R.id.editDiscountCode)
        editCouponDescription = findViewById(R.id.editCouponDescription)
        linearOffer = findViewById(R.id.linearOffer)
        linearMenu = findViewById(R.id.linearMenu)
        txtCouponsLine = findViewById(R.id.txtCouponsLine)
        txtOffersLine = findViewById(R.id.txtOffersLine)
        imgOk = findViewById(R.id.imgOk)
        txtLink = findViewById(R.id.txtLink)

        val sharedPreferences: SharedPreferences = getSharedPreferences("Login Data", Context.MODE_PRIVATE)
        val userName: String = sharedPreferences.getString("userName", "") ?: ""

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        db.collection("users").whereEqualTo("user", userName).limit(1).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    // Get the user ID
                    userId = documentSnapshot.id
                    break // Only need the first result (assuming unique names)
                }
            }.addOnFailureListener { e -> }

        layoutOffers.setOnClickListener {
            linearLink!!.visibility = View.VISIBLE
            txtOffersLine!!.visibility = View.VISIBLE
            txtCouponsLine!!.visibility = View.GONE
        }

        linearCouponButton.setOnClickListener {
            linearCoupon!!.visibility = View.VISIBLE
            linearLink!!.visibility = View.GONE
            txtOffersLine!!.visibility = View.GONE
            txtCouponsLine!!.visibility = View.VISIBLE
        }

        btnNextCoupon!!.setOnClickListener {
            if (editCouponCode?.text.isNullOrEmpty() || editCouponLink?.text.isNullOrEmpty()) {
                Toast.makeText(this@Share, "YOU MUST FILL ALL FIELDS", Toast.LENGTH_SHORT).show()
            } else {
                val discountMap: MutableMap<String, Any?> = HashMap()
                name = editCouponName?.text.toString()
                description = editCouponDescription?.text.toString()
                discountCode = editDiscountCode?.text.toString()
                couponLink = editCouponLink?.text.toString()

                discountMap["name"] = name
                discountMap["link"] = couponLink
                discountMap["description"] = description
                discountMap["userId"] = userId
                discountMap["discountCode"] = discountCode
                discountMap["date"] = FieldValue.serverTimestamp()

                db.collection("discount").add(discountMap)
                    .addOnSuccessListener { documentReference ->
                        Log.d(ContentValues.TAG, "Discount added with ID: ${documentReference.id}")
                    }.addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error adding discount", e)
                    }
                finish()
            }
        }

        btnNextOffer!!.setOnClickListener {
            if (editLink?.text.toString().contains("amazon.es")) {
                linearLink!!.visibility = View.GONE
                linearOffer!!.visibility = View.VISIBLE
                imgOk!!.visibility = View.VISIBLE
                linearMenu!!.visibility = View.GONE

                Thread {
                    try {
                        imgProduct!!.visibility = View.VISIBLE
                        // Perform HTTP request and get HTML content
                        val url: String = editLink?.text.toString()
                        val doc: Document = Jsoup.connect(url).get()
                        val elementImgId: Element = doc.selectFirst("#landingImage")

                        if (elementImgId != null) {
                            val imgUrl: String = elementImgId.attr("src") // Get the "src" attribute of the image
                            runOnUiThread {
                                Picasso.get().load(imgUrl).into(imgProduct)
                                imgText = imgUrl
                                val builder = AlertDialog.Builder(this@Share)
                                builder.setMessage("Do you want to load the product information?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes") { dialog, id ->
                                        // Action to take if user selects "Yes"
                                        val categoryElement: Elements = doc.getElementsByClass("a-link-normal a-color-tertiary")
                                        if (categoryElement.isNotEmpty()) {
                                            val category: String = categoryElement.first().text()
                                            categoryText = category
                                            Log.d("Category", "Category is $category")
                                        } else {
                                            Log.d("Category", "No category found")
                                        }
                                        val elementDesc: Elements = doc.select(".a-spacing-mini")
                                        if (elementDesc.isNotEmpty()) {
                                            editDescription.setText(elementDesc.text())
                                        }
                                        val currentPrice: Element = doc.selectFirst(".a-price-whole")
                                        if (currentPrice != null) {
                                            val currentPriceDecimal: Element = doc.selectFirst(".a-price-fraction")
                                            editCurrentPrice?.setText(
                                                if (currentPriceDecimal != null) {
                                                    "${currentPrice.text()}${currentPriceDecimal.text()}".replace(",", ".")
                                                } else {
                                                    currentPrice.text.replace(",", ".")
                                                }
                                            )
                                        }
                                        val previousPrice: Element = doc.selectFirst(".a-price a-text-price")
                                        if (previousPrice != null) {
                                            editPreviousPrice?.setText(previousPrice.text.replace(",", "."))
                                        }
                                    }
                                    .setNegativeButton("No", null)
                                val alert = builder.create()
                                alert.show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Share", "Could not load image: $e")
                    }
                }.start()
            } else {
                Toast.makeText(this@Share, "THE PAGE MUST BE AMAZON.ES", Toast.LENGTH_SHORT).show()
            }
        }

        imgOk!!.setOnClickListener {
            if (editCurrentPrice?.text.isNullOrEmpty() || editPreviousPrice?.text.isNullOrEmpty() ||
                editName?.text.isNullOrEmpty() || editLink?.text.isNullOrEmpty() || editDescription?.text.isNullOrEmpty()
            ) {
                Toast.makeText(this@Share, "Complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                link = editLink?.text.toString()
                name = editName?.text.toString()
                description = editDescription?.text.toString()
                currentPrice = editCurrentPrice?.text.toString().toDouble()
                previousPrice = editPreviousPrice?.text.toString().toDouble()

                val discountWithoutRounding = (currentPrice!! - previousPrice!!) / previousPrice!! * 100
                val df = java.text.DecimalFormat("#.##")
                discount = df.format(discountWithoutRounding).toDouble()

                val product: MutableMap<String, Any?> = HashMap()
                product["name"] = name
                product["link"] = link
                product["image"] = imgText
                product["description"] = description
                product["previousPrice"] = previousPrice
                product["currentPrice"] = currentPrice
                product["discount"] = discount
                product["userId"] = userId
                product["date"] = FieldValue.serverTimestamp()
                product["likes"] = 0
                product["dislikes"] = 0
                product["sales"] = 0
                product["category"] = categoryText

                db.collection("products").add(product)
                    .addOnSuccessListener { documentReference ->
                        Log.d(ContentValues.TAG, "Product added with ID: ${documentReference.id}")
                    }.addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error adding product", e)
                    }
                finish()
            }
        }
    }

    companion object {
        private const val PICK_IMAGE = 0
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }
}
