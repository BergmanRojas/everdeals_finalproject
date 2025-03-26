package project.mobile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.mobile.controller.ForumViewModel
import project.mobile.model.ForumTopic

@Composable
fun ForumScreen(viewModel: ForumViewModel, navController: NavController) {
    val topics = viewModel.forumTopics.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics.value) { topic ->
            ForumTopicCard(
                topic = topic,
                onClick = { navController.navigate("forum_detail/${topic.id}") }
            )
        }
    }
}

@Composable
fun ForumTopicCard(topic: ForumTopic, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(topic.title, fontWeight = FontWeight.Bold, color = Color.White)
            Text("by ${topic.userName}", fontSize = 12.sp, color = Color.Gray)
            Text("${topic.postCount} posts", fontSize = 12.sp, color = Color(0xFFFF6200))
        }
    }
}