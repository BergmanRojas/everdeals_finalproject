package project.mobile.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import project.mobile.model.Alert
import project.mobile.model.Product
import project.mobile.model.ProductRepository
import project.mobile.model.AuthRepository
import java.util.UUID

class AlertViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    private val _matchingProducts = MutableStateFlow<List<Product>>(emptyList())
    val matchingProducts: StateFlow<List<Product>> = _matchingProducts

    init {
        loadAlerts()
        loadMatchingProducts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            val userAlerts = productRepository.getAlerts(userId)
            _alerts.value = userAlerts
        }
    }

    private fun loadMatchingProducts() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            val products = productRepository.getMatchingProducts(userId)
            _matchingProducts.value = products
        }
    }

    fun createAlert(keyword: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            val newAlert = Alert(
                id = UUID.randomUUID().toString(),
                userId = userId,
                keyword = keyword.trim()
            )
            
            productRepository.createAlert(newAlert).onSuccess {
                loadAlerts()
                loadMatchingProducts()
            }
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            productRepository.deleteAlert(alertId).onSuccess {
                loadAlerts()
                loadMatchingProducts()
            }
        }
    }
} 