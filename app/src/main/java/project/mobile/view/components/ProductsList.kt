package project.mobile.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.mobile.controller.ProductViewModel
import project.mobile.models.Product

@Composable
fun ProductList(
    products: List<Product>,
    dealViewModel: ProductViewModel,
    onProductClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductItem(
                product = product,
                dealViewModel = dealViewModel,
                onProductClick = onProductClick
            )
        }
    }
}