package com.bergman.everdeals_finalproject.models

import com.google.firebase.Timestamp

class Comment(
    var userId: String,
    var productId: String,
    var comment: String,
    time: Timestamp
) {
    private var time: Timestamp

    init {
        this.time = time
    }

    fun getTime(): Timestamp {
        return time
    }

    fun setTime(time: Timestamp) {
        this.time = time
    }
}
