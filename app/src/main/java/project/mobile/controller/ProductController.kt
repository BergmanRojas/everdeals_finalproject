package project.mobile.controller

import androidx.lifecycle.ViewModel
import project.mobile.model.Product
import project.mobile.model.ProductRepository

class ProductController(private val productRepository: ProductRepository) : ViewModel() {

    suspend fun addProduct(product: Product): Result<Unit> {
        return productRepository.addProduct(product)
    }

    suspend fun getProducts(): List<Product> {
        return productRepository.getProducts()
    }
}

