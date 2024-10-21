package com.bergman.everdeals_finalproject.controller

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Comment
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CommentList(comments: List<Comment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen de perfil del usuario
        Image(
            painter = rememberAsyncImagePainter(model = "https://example.com/image.jpg"), // Usa Coil para cargar la imagen
            contentDescription = "Imagen de perfil",
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            // Nombre del usuario
            Text(
                text = comment.userId, // Usa el nombre del usuario aquí
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            // Comentario del usuario
            Text(
                text = comment.comment,
                style = MaterialTheme.typography.bodyMedium
            )

            // Tiempo transcurrido
            val timeElapsed = remember { calculateTimeElapsed(comment.time) }
            Text(
                text = timeElapsed ?: "Recientemente",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Simulamos el tiempo como LocalDateTime en lugar de usar Firebase Timestamp
private fun calculateTimeElapsed(commentTime: LocalDateTime): String? {
    val now = LocalDateTime.now()
    val duration = Duration.between(commentTime, now)
    val hoursElapsed = duration.toHours()

    return when {
        hoursElapsed == 1L -> "Hace una hora ${commentTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        hoursElapsed == 0L -> "Recientemente ${commentTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> "$hoursElapsed horas atrás ${commentTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    }
}

// Datos de ejemplo para la vista previa
@Preview(showBackground = true)
@Composable
fun PreviewCommentItem() {
    val sampleComment = Comment(
        userId = "Usuario123",
        comment = "Este es un comentario de prueba.",
        time = LocalDateTime.now().minusHours(2), // Simula el tiempo actual menos 2 horas
        productId = "producto123" // Proporciona un valor para el productId
    )
    CommentItem(comment = sampleComment)
}

@Preview(showBackground = true)
@Composable
fun PreviewCommentList() {
    val sampleComments = listOf(
        Comment(userId = "Usuario1", comment = "Comentario 1", time = LocalDateTime.now().minusHours(1), productId = "producto1"),
        Comment(userId = "Usuario2", comment = "Comentario 2", time = LocalDateTime.now().minusMinutes(30), productId = "producto2"),
        Comment(userId = "Usuario3", comment = "Comentario 3", time = LocalDateTime.now().minusDays(1), productId = "producto3")
    )
    CommentList(comments = sampleComments)
}
