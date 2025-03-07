package project.mobile.view.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import project.mobile.controller.AuthViewModel
import project.mobile.controller.ProductViewModel
import project.mobile.models.Product
import project.mobile.view.components.ProductList
import project.mobile.view.navigations.Screen

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    dealViewModel: ProductViewModel,
    activity: Activity,
    navController: NavHostController? = null
) {
    val products by dealViewModel.products.collectAsState()

    Scaffold(
        modifier = Modifier.background(Color.White) // Fondo blanco
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (products === emptyList<Product>()) {
                        Text(
                            text = "No products available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                ProductList(
                    products = products,
                    dealViewModel = dealViewModel,
                    onProductClick = { productId ->
                        navController?.navigate(Screen.ViewProduct.createRoute(productId))
                    }
                )
            }
        }
    }
}