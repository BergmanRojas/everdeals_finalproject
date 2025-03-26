package project.mobile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.mobile.formatTimestamp
import project.mobile.model.UserActivity
import project.mobile.ui.theme.OrangeFF6200

@Composable
fun ActivityCard(activity: UserActivity) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activity.type) {
                        "comment" -> "Commented"
                        "product_shared" -> "Shared a Product"
                        "like" -> "Liked a Product"
                        "dislike" -> "Disliked a Product"
                        "forum_created" -> "Created a Forum Topic"
                        else -> "Unknown Activity"
                    },
                    color = OrangeFF6200,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(activity.timestamp),
                    color = secondaryTextColor,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = activity.content,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}