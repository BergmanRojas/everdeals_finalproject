package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

class Share : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShareScreen()
        }
    }
}

@Composable
fun ShareScreen() {
    var imgProductUrl by remember { mutableStateOf("https://example.com/default.jpg") }
    var productName by remember { mutableStateOf(TextFieldValue("")) }
    var productLink by remember { mutableStateOf(TextFieldValue("")) }
    var productDescription by remember { mutableStateOf(TextFieldValue("")) }
    var previousPrice by remember { mutableStateOf(TextFieldValue("")) }
    var currentPrice by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Share a Product", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Image of the product
        Image(
            painter = rememberAsyncImagePainter(imgProductUrl),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fields for entering product details
        BasicTextField(
            value = productLink,
            onValueChange = { productLink = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (productLink.text.isEmpty()) {
                    Text(text = "Product Link", color = Color.Gray)
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = productName,
            onValueChange = { productName = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (productName.text.isEmpty()) {
                    Text(text = "Product Name", color = Color.Gray)
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = productDescription,
            onValueChange = { productDescription = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (productDescription.text.isEmpty()) {
                    Text(text = "Product Description", color = Color.Gray)
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = previousPrice,
            onValueChange = { previousPrice = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (previousPrice.text.isEmpty()) {
                    Text(text = "Previous Price", color = Color.Gray)
                }
                innerTextField()
            }
        )

        BasicTextField(
            value = currentPrice,
            onValueChange = { currentPrice = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (currentPrice.text.isEmpty()) {
                    Text(text = "Current Price", color = Color.Gray)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button for loading product data
        Button(
            onClick = {
                // Load product information from the link (simulated for now)
                coroutineScope.launch {
                    imgProductUrl = "https://example.com/loaded_image.jpg" // Simulated image URL
                    productName = TextFieldValue("Loaded Product Name")
                    productDescription = TextFieldValue("Loaded product description from website.")
                    previousPrice = TextFieldValue("120.00")
                    currentPrice = TextFieldValue("100.00")
                    showDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Product Info")
        }

        // Confirmation dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Confirm Product Info") },
                text = { Text(text = "Do you want to confirm the product information?") },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShareScreen() {
    ShareScreen()
}
