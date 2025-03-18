package project.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import project.mobile.navigation.Navigation
import project.mobile.ui.theme.EverDealsTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import project.mobile.controller.ProductViewModel
import project.mobile.controller.AuthViewModel
import project.mobile.model.ProductRepository
import project.mobile.model.AuthRepository
import project.mobile.util.AmazonScraper
import project.mobile.model.UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EverDealsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val userPreferences = UserPreferences(applicationContext)
                    val authRepository = AuthRepository(userPreferences)
                    val productRepository = ProductRepository(authRepository)
                    val amazonScraper = AmazonScraper()
                    
                    val productViewModel: ProductViewModel = viewModel(
                        factory = ProductViewModel.Factory(
                            application = application,
                            repository = productRepository,
                            amazonScraper = amazonScraper,
                            authRepository = authRepository
                        )
                    )
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.Factory(
                            application = application,
                            authRepository = authRepository,
                            userPreferences = userPreferences
                        )
                    )

                    Navigation(
                        navController = navController,
                        productViewModel = productViewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

