package project.mobile.view

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import project.mobile.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen() {
    var productName by remember { mutableStateOf("") }
    var productImage by remember { mutableStateOf("") }
    var productLink by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(productName, productImage, productLink, productPrice) {
        isValid = productName.isNotEmpty() && productImage.isNotEmpty() && productLink.isNotEmpty() && productPrice.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share a Deal") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = productImage,
                onValueChange = { productImage = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = productLink,
                onValueChange = { productLink = it },
                label = { Text("Product Link") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = productPrice,
                onValueChange = { productPrice = it },
                label = { Text("Price (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out this deal: $productName for €$productPrice! $productLink")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            ) {
                Text("Share Deal")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (productImage.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(productImage),
                    contentDescription = "Product Image",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShareScreen() {
    ShareScreen()
}

