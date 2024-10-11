package com.bergman.everdeals_finalproject.controller

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CustomCommentAdapter(var context: Context, comments: ArrayList<Comment>) :
    ArrayAdapter<Comment?>(context, -1, comments) {
    var comments: ArrayList<Comment> = comments
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowComment: View = inflater.inflate(R.layout.activity_item_comment, parent, false)
        val txtComment = rowComment.findViewById<TextView>(R.id.txtComment)
        val txtUser = rowComment.findViewById<TextView>(R.id.txtUserComment)
        val txtTime = rowComment.findViewById<TextView>(R.id.txtTimeComment)
        val imgUser = rowComment.findViewById<ImageView>(R.id.imgProfileComment)
        val comment: Comment = comments[position]

        try {
            txtComment.text = comment.getComment()
            val timeElapsed = calculateTimeElapsed(comment.getTime())
            txtTime.text = timeElapsed
        } catch (error: Exception) {
            println(error.toString() + " - error")
        }

        db.collection("users").whereEqualTo("userId", comment.getUserId()).limit(1)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot in queryDocumentSnapshots) {
                    // Get the user's name
                    val userName: String? = documentSnapshot.getString("user")
                    txtUser.text = userName
                    val imgUrl =
                        "https://firebasestorage.googleapis.com/v0/b/proyecto-descuentos-26422.appspot.com/o/carpeta_imagenes%2F$userName.jpg?alt=media&token=8aea3353-10c8-4f93-9ece-caed6ddb1a87"
                    Picasso.get().load(imgUrl).into(imgUser)
                    break // Only need to get the first result (assuming unique names)
                }
            }.addOnFailureListener { e -> }

        return rowComment
    }

    private fun calculateTimeElapsed(timestamp: Timestamp): String? {
        var specificDate: LocalDateTime? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            specificDate = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
        val now: LocalDateTime? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else null

        val duration: Duration? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Duration.between(specificDate, now)
        } else null

        var hoursElapsed: Long = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hoursElapsed = duration!!.toHours()
        }

        var result: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = if (hoursElapsed == 1L) {
                "An hour ago " + specificDate!!.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
            } else if (hoursElapsed == 0L) {
                "Recently shared " + specificDate!!.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
            } else {
                "$hoursElapsed hours ago " + specificDate!!.format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
            }
        }
        return result
    }
}
