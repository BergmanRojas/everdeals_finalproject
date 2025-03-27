package project.mobile.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import project.mobile.model.Alert
import project.mobile.model.Product
import project.mobile.controller.AlertViewModel
import project.mobile.view.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertViewModel,
    onProductClick: (String) -> Unit
) {
    var showAddAlertDialog by remember { mutableStateOf(false) }
    var newKeyword by remember { mutableStateOf("") }
    
    val alerts by viewModel.alerts.collectAsState()
    val matchingProducts by viewModel.matchingProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Alerts") },
                actions = {
                    IconButton(onClick = { showAddAlertDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add alert")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Active alerts list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Active Keywords",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    alerts.forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(alert.keyword)
                            IconButton(
                                onClick = { viewModel.deleteAlert(alert.id) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete alert",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Matching products list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (matchingProducts.isEmpty()) {
                    item {
                        Text(
                            "No products match your alerts yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(matchingProducts) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
            }
        }

        // Add alert dialog
        if (showAddAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAddAlertDialog = false },
                title = { Text("Add New Alert") },
                text = {
                    OutlinedTextField(
                        value = newKeyword,
                        onValueChange = { newKeyword = it },
                        label = { Text("Keyword") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKeyword.isNotBlank()) {
                                viewModel.createAlert(newKeyword)
                                newKeyword = ""
                                showAddAlertDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAlertDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
} 