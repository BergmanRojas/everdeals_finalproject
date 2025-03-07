package project.mobile.view.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.controller.AuthViewModel
import project.mobile.controller.ProductViewModel
import project.mobile.models.AuthState
import project.mobile.models.Category
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrapingState by productViewModel.scrapingState.collectAsState()

    var currentStep by remember { mutableStateOf(0) }
    var productName by remember { mutableStateOf("") }
    var amazonUrl by remember { mutableStateOf("") }
    var productImage by remember { mutableStateOf("") }
    var productLink by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(true) }
    var showImageSelector by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState !is AuthState.Success) {
            snackbarHostState.showSnackbar("Please log in to share a deal")
            onNavigateBack()
        }
    }

    fun shareAndSaveProduct() {
        scope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    productViewModel.addProduct(
                        name = productName,
                        imageUrl = productImage,
                        amazonUrl = amazonUrl, // Mapea 'link' a 'amazonUrl'
                        currentPrice = currentPrice.toDoubleOrNull() ?: 0.0,
                        originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                        description = description,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        isOnline = isOnline,
                        userId = currentUser.uid,
                        userName = currentUser.displayName ?: "Anonymous",
                        userPhotoUrl = currentUser.photoUrl?.toString() ?: "",
                        createdAt = Timestamp.now(),
                        comments = emptyList()
                    )
                    snackbarHostState.showSnackbar("Deal shared successfully!")

                    // Intent para compartir
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out this deal: $productName for €$currentPrice! $productLink")
                    }
                    try {
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error sharing: ${e.message}")
                    }

                    onNavigateBack()
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Error sharing deal: ${e.message}")
                }
            } else {
                snackbarHostState.showSnackbar("Error: User not authenticated")
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share a Deal", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
                .padding(padding)
        ) {
            when (currentStep) {
                0 -> AmazonLinkScreen(
                    amazonUrl = amazonUrl,
                    onUrlChange = { amazonUrl = it },
                    onNext = { productViewModel.scrapeAmazonProduct(amazonUrl); currentStep++ }
                )
                1 -> ImageSelectionScreen(
                    scrapingState = scrapingState,
                    onImageSelected = { productImage = it; currentStep++ }
                )
                2 -> ProductDetailsScreen(
                    productName = productName,
                    onProductNameChange = { productName = it },
                    currentPrice = currentPrice,
                    onCurrentPriceChange = { currentPrice = it },
                    originalPrice = originalPrice,
                    onOriginalPriceChange = { originalPrice = it },
                    productLink = productLink,
                    onProductLinkChange = { productLink = it },
                    isOnline = isOnline,
                    onIsOnlineChange = { isOnline = it },
                    onNext = { currentStep++ }
                )
                3 -> DescriptionScreen(
                    description = description,
                    onDescriptionChange = { description = it },
                    onNext = { currentStep++ }
                )
                4 -> FinalDetailsScreen(
                    category = category,
                    onCategoryChange = { category = it },
                    startDate = startDate,
                    onStartDateChange = { startDate = it },
                    endDate = endDate,
                    onEndDateChange = { endDate = it },
                    showStartDatePicker = showStartDatePicker,
                    onShowStartDatePickerChange = { showStartDatePicker = it },
                    showEndDatePicker = showEndDatePicker,
                    onShowEndDatePickerChange = { showEndDatePicker = it },
                    onSubmit = { shareAndSaveProduct() }
                )
            }
        }

        // Date Picker Dialogs
        if (showStartDatePicker) {
            val startDatePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDatePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                                startDate = formattedDate
                            }
                            showStartDatePicker = false
                        }
                    ) {
                        Text("OK", color = Color(0xFF9D4EDD))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStartDatePicker = false }
                    ) {
                        Text("Cancel", color = Color(0xFF9D4EDD))
                    }
                }
            ) {
                DatePicker(
                    state = startDatePickerState,
                    showModeToggle = false
                )
            }
        }
        if (showEndDatePicker) {
            val endDatePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDatePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                                endDate = formattedDate
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("OK", color = Color(0xFF9D4EDD))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEndDatePicker = false }
                    ) {
                        Text("Cancel", color = Color(0xFF9D4EDD))
                    }
                }
            ) {
                DatePicker(
                    state = endDatePickerState,
                    showModeToggle = false
                )
            }
        }

        // Image Selector Dialog
        if (showImageSelector) {
            AlertDialog(
                onDismissRequest = { showImageSelector = false },
                title = { Text("Select an Image", color = Color.White) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (scrapingState) {
                            is ProductViewModel.ScrapingState.Success -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.height(300.dp)
                                ) {
                                    items((scrapingState as ProductViewModel.ScrapingState.Success).imageUrls) { imageUrl ->
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Scraped Image",
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clickable {
                                                    productImage = imageUrl
                                                    showImageSelector = false
                                                }
                                                .border(1.dp, Color(0xFF9D4EDD), RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            is ProductViewModel.ScrapingState.Loading -> {
                                CircularProgressIndicator(color = Color(0xFF9D4EDD))
                            }
                            is ProductViewModel.ScrapingState.Error -> {
                                Text(
                                    text = "Error: ${(scrapingState as ProductViewModel.ScrapingState.Error).message}",
                                    color = Color.Red
                                )
                            }
                            else -> {}
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImageSelector = false }) {
                        Text("Cancel", color = Color(0xFF9D4EDD))
                    }
                },
                containerColor = Color(0xFF2A2A2A),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmazonLinkScreen(
    amazonUrl: String,
    onUrlChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Share a Deal with Millions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = amazonUrl,
            onValueChange = onUrlChange,
            label = { Text("Amazon Product URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                if (amazonUrl.isNotEmpty()) {
                    IconButton(onClick = onNext) {
                        Text("Scrape", color = Color(0xFF9D4EDD))
                    }
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Next")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    scrapingState: ProductViewModel.ScrapingState,
    onImageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select an Image for Your Product",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        when (scrapingState) {
            is ProductViewModel.ScrapingState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scrapingState.imageUrls) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Scraped Image",
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onImageSelected(imageUrl) }
                                .border(1.dp, Color(0xFF9D4EDD), RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            is ProductViewModel.ScrapingState.Loading -> {
                CircularProgressIndicator(color = Color(0xFF9D4EDD))
            }
            is ProductViewModel.ScrapingState.Error -> {
                Text(
                    text = "Error: ${scrapingState.message}",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {}
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onImageSelected("") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Skip")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productName: String,
    onProductNameChange: (String) -> Unit,
    currentPrice: String,
    onCurrentPriceChange: (String) -> Unit,
    originalPrice: String,
    onOriginalPriceChange: (String) -> Unit,
    productLink: String,
    onProductLinkChange: (String) -> Unit,
    isOnline: Boolean,
    onIsOnlineChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Product Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                OutlinedTextField(
                    value = productName,
                    onValueChange = onProductNameChange,
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = Color(0xFF9D4EDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF9D4EDD),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = currentPrice,
                    onValueChange = onCurrentPriceChange,
                    label = { Text("Current Price (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = Color(0xFF9D4EDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF9D4EDD),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = originalPrice,
                    onValueChange = onOriginalPriceChange,
                    label = { Text("Original Price (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = Color(0xFF9D4EDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF9D4EDD),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = productLink,
                    onValueChange = onProductLinkChange,
                    label = { Text("Product Link") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = Color(0xFF9D4EDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF9D4EDD),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Availability: ", color = Color.White)
                    Switch(
                        checked = isOnline,
                        onCheckedChange = onIsOnlineChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF9D4EDD),
                            checkedTrackColor = Color(0xFF9D4EDD).copy(alpha = 0.5f)
                        )
                    )
                    Text(if (isOnline) "Online" else "In-store", color = Color.White)
                }
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Next")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionScreen(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Product Description",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Next")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalDetailsScreen(
    category: String,
    onCategoryChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    showStartDatePicker: Boolean,
    onShowStartDatePickerChange: (Boolean) -> Unit,
    showEndDatePicker: Boolean,
    onShowEndDatePickerChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("categories")
                    .get()
                    .await()
                categories = snapshot.toObjects(Category::class.java)
            } catch (e: Exception) {
                categories = emptyList()
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Final Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF9D4EDD))
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { /* No editable directamente */ },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = Color(0xFF9D4EDD),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF9D4EDD),
                        unfocusedLabelColor = Color.Gray
                    ),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select category",
                                tint = Color(0xFF9D4EDD)
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A))
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = Color.White) },
                            onClick = {
                                onCategoryChange(cat.name)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = startDate,
            onValueChange = onStartDateChange,
            label = { Text("Start Date") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(onClick = { onShowStartDatePickerChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select start date",
                        tint = Color(0xFF9D4EDD)
                    )
                }
            },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = endDate,
            onValueChange = onEndDateChange,
            label = { Text("End Date") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(onClick = { onShowEndDatePickerChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select end date",
                        tint = Color(0xFF9D4EDD)
                    )
                }
            },
            readOnly = true
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Submit")
        }
    }
}