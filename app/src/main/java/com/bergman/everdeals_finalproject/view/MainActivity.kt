package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Product
import com.bergman.everdeals_finalproject.ui.theme.EverdealsFinalProjectTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val products: MutableState<List<Product>> = mutableStateOf(emptyList())

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EverdealsFinalProjectTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(onItemClicked = { /* Navegación a diferentes pantallas */ })
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Everdeals") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            SectionSelector(products = products.value)
                            ProductsList(products = products.value)

                            LaunchedEffect(Unit) {
                                products.value = loadProducts()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadProducts(): List<Product> {
        return listOf(
            Product("Producto 1", "https://example.com/image1.jpg", "Descripción 1", "url", "userId", "category", "id1", 100.0, 80.0, 20.0, 10, 1, 50),
            Product("Producto 2", "https://example.com/image2.jpg", "Descripción 2", "url", "userId", "category", "id2", 200.0, 150.0, 25.0, 20, 5, 30)
        )
    }
}

@Composable
fun SectionSelector(products: List<Product>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Populares", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
        Text(text = "Destacados", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
        Text(text = "Nuevos", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
    }
}

@Composable
fun ProductsList(products: List<Product>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(products) { product ->
            ProductItem(product = product)
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { }
    ) {
        Text(
            text = product.name,  // Accede a la propiedad directamente
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Image(
            painter = rememberAsyncImagePainter(product.productImage),  // Accede a la propiedad directamente
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Price: ${product.currentPrice}€",  // Accede a la propiedad directamente
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun DrawerContent(onItemClicked: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = "https://example.com/image.jpg"),
                contentDescription = "Logo",
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = "CHOLLAZOS",
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Categorías", modifier = Modifier.clickable { onItemClicked("categorías") })
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Ajustes", modifier = Modifier.clickable { onItemClicked("ajustes") })
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Foro de discusión", modifier = Modifier.clickable { onItemClicked("foro") })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EverdealsFinalProjectTheme {
        val sampleProduct = Product(
            name = "Producto de Ejemplo",
            productImage = "https://example.com/image.jpg",
            description = "Este es un producto de ejemplo.",
            link = "https://example.com",
            userId = "user123",
            category = "categoría",
            id = "id123",
            previousPrice = 120.0,
            currentPrice = 99.0,
            discount = 20.0,
            likes = 50,
            dislikes = 10,
            sales = 100
        )
        ProductItem(product = sampleProduct)
    }
}



