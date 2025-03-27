package project.mobile.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.mobile.controller.ProductViewModel
import project.mobile.model.AffiliateStats
import project.mobile.model.Transaction
import project.mobile.model.TransactionStatus
import project.mobile.ui.theme.OrangeFF6200
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffiliateScreen(
    productViewModel: ProductViewModel
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showTransactionHistory by remember { mutableStateOf(false) }
    
    val affiliateStats by productViewModel.affiliateStats.collectAsState()
    val transactions by productViewModel.transactions.collectAsState()
    val withdrawals by productViewModel.withdrawals.collectAsState()
    val userBalance by productViewModel.userBalance.collectAsState()
    var totalClicks = 0
    var totalSales = 0
    var totalEarnings = 0.0
    var totalWithdrawn = 0.0

    affiliateStats.forEach { stats ->
        totalClicks += stats.clicks
        totalSales += stats.sales
        totalEarnings += stats.earnings
    }

    withdrawals.forEach { withdrawal ->
        if (withdrawal["status"] == "COMPLETED") {
            totalWithdrawn += (withdrawal["amount"] as? Double ?: 0.0)
        }
    }

    val netEarnings = totalEarnings - totalWithdrawn

    if (showWithdrawDialog) {
        WithdrawDialog(
            availableBalance = netEarnings,
            onDismiss = { showWithdrawDialog = false },
            onWithdraw = { amount, paypalEmail ->
                productViewModel.requestWithdrawal(amount, paypalEmail)
                showWithdrawDialog = false
            }
        )
    }

    if (showTransactionHistory) {
        TransactionHistoryDialog(
            transactions = transactions,
            onDismiss = { showTransactionHistory = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Affiliate Dashboard",
                        color = OrangeFF6200,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { showTransactionHistory = true }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Transaction History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showWithdrawDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Withdraw",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Earnings",
                        value = "$${String.format("%.2f", netEarnings)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Clicks",
                        value = totalClicks.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Sales",
                        value = totalSales.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Lista de productos
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(affiliateStats) { stats ->
                ProductStatsCard(stats = stats)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = if (title == "Total Earnings") OrangeFF6200 else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (value.length > 8) 16.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProductStatsCard(stats: AffiliateStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(stats.productImage)
                    .crossfade(true)
                    .build(),
                contentDescription = stats.productName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stats.productName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", stats.productPrice)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Clicks
                    Column {
                        Text(
                            text = "Clicks",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${stats.clicks}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Ventas
                    Column {
                        Text(
                            text = "Sales",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${stats.sales}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Ganancias
                    Column {
                        Text(
                            text = "Earnings",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", stats.earnings)}",
                            color = OrangeFF6200,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawDialog(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onWithdraw: (amount: Double, paypalEmail: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var paypalEmail by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Withdraw Earnings",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Available Balance: $${String.format("%.2f", availableBalance)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to Withdraw") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$") }
                )
                OutlinedTextField(
                    value = paypalEmail,
                    onValueChange = { paypalEmail = it },
                    label = { Text("PayPal Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    when {
                        amountValue == null -> error = "Please enter a valid amount"
                        amountValue <= 0 -> error = "Amount must be greater than 0"
                        amountValue > availableBalance -> error = "Amount exceeds available balance"
                        paypalEmail.isEmpty() -> error = "Please enter your PayPal email"
                        !paypalEmail.contains("@") -> error = "Please enter a valid email"
                        else -> onWithdraw(amountValue, paypalEmail)
                    }
                }
            ) {
                Text("Withdraw")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransactionHistoryDialog(
    transactions: List<Transaction>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction.status) {
                TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.paypalEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transaction.createdAt.toDate().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = { },
                label = { Text(transaction.status.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (transaction.status) {
                        TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                        TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                        TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            )
        }
    }
} 