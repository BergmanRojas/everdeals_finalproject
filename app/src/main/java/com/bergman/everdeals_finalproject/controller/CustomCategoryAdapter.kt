package com.bergman.everdeals_finalproject.controller

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso

class CustomCategoryAdapter(var context: Context, categories: ArrayList<Category>) :
    ArrayAdapter<Category?>(context, -1, categories) {
    var categories: ArrayList<Category> = categories

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowCategory: View = inflater.inflate(R.layout.activity_item_category, parent, false)
        val txtName = rowCategory.findViewById<TextView>(R.id.txtCategory)
        val imgCategory = rowCategory.findViewById<ImageView>(R.id.imgCategory)
        val linearCategory: LinearLayout =
            rowCategory.findViewById(R.id.layoutCategoryActivity)
        val category: Category = categories[position]

        txtName.text = category.getName() // Update method call to getName()
        Picasso.get().load(category.getImageUrl()).into(imgCategory) // Update method call to getImageUrl()

        linearCategory.setOnClickListener {
            val intent = Intent(context, ViewCategory::class.java) // Update target class
            intent.putExtra("category", category.getName()) // You can pass additional information to the activity if necessary
            context.startActivity(intent)
        }
        return rowCategory
    }
}
