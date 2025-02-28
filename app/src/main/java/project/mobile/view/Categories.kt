package project.mobile.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.controller.CategoryAdapter // ✅ Usamos el nombre correcto
import project.mobile.models.Category

class Categories : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val categories = remember { mutableStateListOf<Category>() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        categories.clear()
                        val db = FirebaseFirestore.getInstance()
                        val result = db.collection("categories").get().await()
                        for (document in result.documents) {
                            val category = document.toObject(Category::class.java)
                            if (category != null) {
                                categories.add(category)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error fetching categories", e)
                    }
                }
            }

            CategoriesScreen(navController = null, categories = categories, onCategoryClick = { category ->
                println("Clicked on category: ${category.name}")
            })
        }
    }
}

@Composable
fun CategoriesScreen(navController: NavController?, categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // ✅ Ahora usamos la función correcta desde `controller`
        CategoryAdapter(categories = categories, onCategoryClick = { category ->
            navController?.navigate("viewCategory/${category.name}")
        })
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoriesScreen() {
    val sampleCategories = listOf(
        Category(imageUrl = "https://example.com/image1.jpg", name = "Technology"),
        Category(imageUrl = "https://example.com/image2.jpg", name = "Fashion"),
        Category(imageUrl = "https://example.com/image3.jpg", name = "Home")
    )
    CategoriesScreen(navController = null, categories = sampleCategories, onCategoryClick = {})
}