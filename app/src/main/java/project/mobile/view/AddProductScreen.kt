package project.mobile.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.mobile.controller.ProductViewModel
import coil.compose.AsyncImage
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import project.mobile.model.Product
import java.util.UUID
import android.widget.Toast
import project.mobile.controller.AuthManager
import project.mobile.model.AuthState  // Añade esta línea
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit,
    productViewModel: ProductViewModel,
    authManager: AuthManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Verificar la autenticación cuando se carga la pantalla
    LaunchedEffect(Unit) {
        val isAuthenticated = authManager.checkSession()
        if (!isAuthenticated) {
            Toast.makeText(context, "Please log in to add a product", Toast.LENGTH_LONG).show()
            onNavigateBack()
        }
    }

    // Monitorear cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar("Authentication error: ${(authState as AuthState.Error).message}")
                onNavigateBack()
            }
            is AuthState.Idle -> {
                snackbarHostState.showSnackbar("Please log in to add a product")
                onNavigateBack()
            }
            else -> {}
        }
    }

    var currentStep by remember { mutableStateOf(0) }
    var amazonUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var discountCode by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(true) }
    var selectedImageUrl by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val scrapingState by productViewModel.scrapingState.collectAsState()


    fun createProduct() {
        scope.launch {
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                val product = Product(
                    id = UUID.randomUUID().toString(),
                    name = title,
                    description = description,
                    originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                    currentPrice = currentPrice.toDoubleOrNull() ?: 0.0,
                    imageUrl = selectedImageUrl,
                    userId = currentUser.id,
                    userName = currentUser.username,
                    userPhotoUrl = currentUser.photoUrl ?: "",
                    amazonUrl = amazonUrl,
                    startDate = startDate,
                    endDate = endDate,
                    category = category,
                    createdAt = Timestamp.now(),
                    likes = 0,
                    dislikes = 0,
                    likedBy = emptyList(),
                    dislikedBy = emptyList()
                )

                try {
                    productViewModel.addProduct(product)
                    Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                    onProductAdded()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error adding product: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Error: User not authenticated", Toast.LENGTH_LONG).show()
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
                .padding(padding)
        ) {
            when (currentStep) {
                0 -> SelectTypeScreen(onNext = { currentStep++ })
                1 -> AmazonLinkScreen(
                    amazonUrl = amazonUrl,
                    onUrlChange = { amazonUrl = it },
                    onNext = {
                        productViewModel.scrapeAmazonProduct(amazonUrl)
                        currentStep++
                    }
                )
                2 -> ImageSelectionScreen(
                    scrapingState = scrapingState,
                    onImageSelected = {
                        selectedImageUrl = it
                        currentStep++
                    }
                )
                3 -> ProductDetailsScreen(
                    title = title,
                    onTitleChange = { title = it },
                    currentPrice = currentPrice,
                    onCurrentPriceChange = { currentPrice = it },
                    originalPrice = originalPrice,
                    onOriginalPriceChange = { originalPrice = it },
                    discountCode = discountCode,
                    onDiscountCodeChange = { discountCode = it },
                    isOnline = isOnline,
                    onIsOnlineChange = { isOnline = it },
                    onNext = { currentStep++ }
                )
                4 -> DescriptionScreen(
                    description = description,
                    onDescriptionChange = { description = it },
                    onNext = { currentStep++ }
                )
                5 -> FinalDetailsScreen(
                    startDate = startDate,
                    onStartDateChange = { startDate = it },
                    endDate = endDate,
                    onEndDateChange = { endDate = it },
                    category = category,
                    onCategoryChange = { category = it },
                    onSubmit = { createProduct() }
                )
            }
        }
    }
}

@Composable
fun SelectTypeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What would you like to share?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SelectTypeCard(
            title = "Product Deal",
            description = "Share specific product deals with or without discount codes",
            icon = Icons.Default.LocalOffer,
            onClick = onNext
        )

        SelectTypeCard(
            title = "Store Coupon",
            description = "Share discount codes that work for multiple products",
            icon = Icons.Default.Discount,
            onClick = onNext
        )

        SelectTypeCard(
            title = "Discussion",
            description = "Start a conversation about deals and savings",
            icon = Icons.Default.Forum,
            onClick = onNext
        )
    }
}

@Composable
fun SelectTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9D4EDD),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
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
            .padding(16.dp)
    ) {
        Text(
            text = "Share a deal with millions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = amazonUrl,
            onValueChange = onUrlChange,
            label = { Text("Amazon product URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Let's begin!")
        }
    }
}

@Composable
fun ImageSelectionScreen(
    scrapingState: ProductViewModel.ScrapingState,
    onImageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select an image for your product",
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
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Product image",
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clickable { onImageSelected(imageUrl) }
                        )
                    }
                }
            }
            is ProductViewModel.ScrapingState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFF9D4EDD)
                )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    title: String,
    onTitleChange: (String) -> Unit,
    currentPrice: String,
    onCurrentPriceChange: (String) -> Unit,
    originalPrice: String,
    onOriginalPriceChange: (String) -> Unit,
    discountCode: String,
    onDiscountCodeChange: (String) -> Unit,
    isOnline: Boolean,
    onIsOnlineChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Product Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currentPrice,
            onValueChange = onCurrentPriceChange,
            label = { Text("Current Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = originalPrice,
            onValueChange = onOriginalPriceChange,
            label = { Text("Original Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = discountCode,
            onValueChange = onDiscountCodeChange,
            label = { Text("Discount Code") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Availability:", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
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
fun DescriptionScreen(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                disabledContainerColor = Color(0xFF2A2A2A),
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
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Final Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = startDate,
            onValueChange = onStartDateChange,
            label = { Text("Start Date") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
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
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = Color(0xFF9D4EDD),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF9D4EDD),
                unfocusedLabelColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(onClick = { showEndDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select end date",
                        tint = Color(0xFF9D4EDD)
                    )
                }
            },
            readOnly = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
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
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
        ) {
            Text("Submit")
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = {
                onStartDateChange(it)
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = {
                onEndDateChange(it)
                showEndDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val datePickerState = rememberDatePickerState()

        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Use SimpleDateFormat instead of LocalDate for better compatibility
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(millis))
                        onDateSelected(date)
                    }
                    onDismissRequest()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    } else {
        // Fallback for older Android versions
        val context = LocalContext.current
        val calendar = Calendar.getInstance()

        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

