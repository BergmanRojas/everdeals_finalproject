package com.bergman.everdeals_finalproject.models

import java.time.LocalDateTime

class Comment(
    var userId: String,
    var productId: String,
    var comment: String,
    var time: LocalDateTime // Usamos LocalDateTime en lugar de Timestamp
)

