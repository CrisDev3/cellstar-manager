package com.example.cs_manager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.data.Product
import com.example.cs_manager.navigation.Routes

/**
 * Pantalla de Catálogo de Inventario moderna e interactiva.
 * Permite buscar productos por SKU, nombre o modelo y muestra badges dinámicos de stock.
 * Provee un FAB (+) en la esquina inferior para dar de alta nuevos productos.
 */
@Composable
fun InventoryScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var sortBy by remember { mutableStateOf("Nombre") }
    var sortExpanded by remember { mutableStateOf(false) }
    var managingProduct by remember { mutableStateOf<Product?>(null) }

    // Colores corporativos
    val primaryColor = Color(0xFF1E3A8A)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    val categories = remember {
        listOf("Todos") + listOf("Celulares", "Accesorios", "Redes", "Hardware", "Audio")
    }
    val sortOptions = listOf("Nombre", "Precio: Menor a Mayor", "Precio: Mayor a Menor", "Stock: Bajo Primero")

    // Filtrar y ordenar productos del repositorio de forma reactiva
    val filteredProducts = CellstarRepository.products
        .filter { product ->
            (selectedCategory == "Todos" || product.category.equals(selectedCategory, ignoreCase = true)) &&
            (product.name.contains(searchQuery, ignoreCase = true) ||
             product.sku.contains(searchQuery, ignoreCase = true) ||
             product.model.contains(searchQuery, ignoreCase = true))
        }
        .sortedWith(
            when (sortBy) {
                "Precio: Menor a Mayor" -> compareBy { it.price }
                "Precio: Mayor a Menor" -> compareByDescending { it.price }
                "Stock: Bajo Primero" -> compareBy { it.stock }
                else -> compareBy { it.name }
            }
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar SKU, nombre o modelo...", color = Color.LightGray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chips de filtro por categorías
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.1f),
                            selectedLabelColor = primaryColor
                        )
                    )
                }
            }

            // Barra de ordenamiento
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { sortExpanded = true }) {
                    Text("Ordenar por: $sortBy", color = primaryColor, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sortBy = option
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron productos en el inventario", color = textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp), // Espacio para el FAB y BottomBar
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductInventoryCard(product = product, onManageClick = { managingProduct = product })
                    }
                }
            }
        }

        // Botón Flotante (FAB +) para Agregar Producto
        FloatingActionButton(
            onClick = { navController.navigate(Routes.ADD_PRODUCT) },
            containerColor = primaryColor,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Producto",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Diálogo interactivo para administrar stock
    if (managingProduct != null) {
        val product = managingProduct!!
        var newStockStr by remember(product.sku) { mutableStateOf(product.stock.toString()) }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { managingProduct = null },
            title = { Text("Administrar Stock - ${product.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SKU: ${product.sku}\nModelo: ${product.model}", color = textSecondary)
                    OutlinedTextField(
                        value = newStockStr,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                newStockStr = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Stock Disponible") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stockVal = newStockStr.toIntOrNull()
                        if (stockVal == null) {
                            errorMessage = "Ingrese un stock válido"
                        } else {
                            val index = CellstarRepository.products.indexOfFirst { it.sku == product.sku }
                            if (index != -1) {
                                val currentProd = CellstarRepository.products[index]
                                CellstarRepository.products[index] = currentProd.copy(stock = stockVal)
                                CellstarRepository.logAction(
                                    CellstarRepository.currentLoggedUser.value,
                                    "STOCK MANUAL UPDATE - ${product.name} (Nuevo stock: $stockVal)",
                                    "blue"
                                )
                            }
                            managingProduct = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { managingProduct = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta individual para mostrar un producto en el listado del inventario.
 */
@Composable
fun ProductInventoryCard(product: Product, onManageClick: () -> Unit) {
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    // Colores para el badge de stock
    val (statusText, badgeColor) = when {
        product.stock == 0 -> "Out of Stock" to Color(0xFFEF4444)
        product.stock < product.minStock -> "Low Stock" to Color(0xFFF59E0B)
        else -> "In Stock" to Color(0xFF10B981)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila Superior: SKU y Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.sku,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                )

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = statusText,
                        color = badgeColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nombre y Modelo
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            )
            Text(
                text = "Model: ${product.model}",
                style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Fila Inferior: Cantidad y Botón Administrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Qty On Hand",
                        style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                    )
                    Text(
                        text = "${product.stock}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (product.stock < product.minStock) Color(0xFFEF4444) else textPrimary
                        )
                    )
                }

                Button(
                    onClick = onManageClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Manage",
                        color = textPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
