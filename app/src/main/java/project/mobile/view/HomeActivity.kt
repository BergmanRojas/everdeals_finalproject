package project.mobile.view

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import project.mobile.R
import project.mobile.controller.DealRepository
import project.mobile.controller.DealViewModel
import project.mobile.controller.DealViewModelFactory
import project.mobile.controller.UserAuthViewModel
import project.mobile.controller.UserRepository
import project.mobile.controller.UserSessionManager
import project.mobile.models.AuthState
import project.mobile.models.Product
import project.mobile.tools.SessionStorage

@OptIn(ExperimentalMaterial3Api::class)
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userRepository = UserRepository()
            val sessionStorage = SessionStorage(applicationContext)
            val sessionManager = UserSessionManager(userRepository, sessionStorage)
            val dealRepository = DealRepository()
            val dealViewModel: DealViewModel = viewModel(
                factory = DealViewModelFactory(application, dealRepository)
            )
            val authViewModel: UserAuthViewModel = viewModel()

            HomeScreen(authViewModel, sessionManager, dealViewModel, this)
        }
    }
}

@Composable
fun HomeScreen(
    authViewModel: UserAuthViewModel,
    sessionManager: UserSessionManager,
    dealViewModel: DealViewModel,
    activity: Activity
) {
    val userState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Scaffold(
            topBar = { TopBar(userState, sessionManager, scope, activity) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                HomePage(dealViewModel)
            }
        }
    }
}

@Composable
fun TopBar(userState: AuthState, sessionManager: UserSessionManager, scope: CoroutineScope, activity: Activity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logoapp),
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            when (userState) {
                is AuthState.Success -> {
                    IconButton(onClick = {
                        scope.launch {
                            sessionManager.signOut()
                            activity.recreate() // Reinicia la actividad para reflejar el cambio de estado
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Sign Out", tint = Color.Red)
                    }
                }
                else -> {
                    Text("Not Logged In", color = Color.Gray)
                }
            }
        }

        Divider(color = Color.Gray, thickness = 1.dp)
    }
}

@Composable
fun HomePage(dealViewModel: DealViewModel) {
    val products by dealViewModel.products.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(products) { product ->
            ProductItem(product, dealViewModel)
        }
    }
}

@Composable
fun ProductItem(product: Product, dealViewModel: DealViewModel) {
    var likeCount by remember { mutableStateOf(product.likes) }
    var dislikeCount by remember { mutableStateOf(product.dislikes) }
    val userId = "current_user_id"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(product.productImage),
                contentDescription = "Product Image",
                modifier = Modifier.size(80.dp)
            )
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "€${product.currentPrice}", color = Color.Red)
                Text(text = "Old Price: €${product.originalPrice}", color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val fakeRepository = DealRepository()
    val fakeViewModel = DealViewModel(
        application = LocalContext.current.applicationContext as Application,
        repository = fakeRepository
    )

    HomePage(dealViewModel = fakeViewModel)
}



