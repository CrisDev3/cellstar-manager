package com.example.cs_manager.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.data.CartItem

/**
 * Pantalla de Punto de Venta (Gestión de Ventas) premium y completamente interactiva.
 * Permite buscar/escanear SKU, ver autocompletado, gestionar el carrito de compras en tiempo real,
 * recopilar información del cliente y procesar el pago lanzando el Intent de facturación externa.
 */
@Composable
fun SalesScreen(navController: NavController) {
    val context = LocalContext.current

    // Estados de entrada
    var skuInput by remember { mutableStateOf("") }
    var clientId by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }

    // Colores corporativos
    val primaryColor = Color(0xFF1E3A8A)
    val secondaryColor = Color(0xFF3B82F6)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val alertColor = Color(0xFFEF4444)

    // Estados reactivos de datos del repositorio
    val cartItems = CellstarRepository.cart
    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }
    val totalCount = cartItems.sumOf { it.quantity }

    // Autocompletado reactivo de SKU a medida que escribe
    val skuSuggestions = if (skuInput.isBlank()) emptyList() else {
        CellstarRepository.products.filter {
            it.sku.contains(skuInput, ignoreCase = true) || it.name.contains(skuInput, ignoreCase = true)
        }.take(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // SECCIÓN 1: Escanear / Agregar SKU
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESCANEAR SKU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = skuInput,
                            onValueChange = { skuInput = it },
                            placeholder = { Text("Ingresar código SKU...", color = Color.LightGray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    // Simula un escaneo seleccionando un producto aleatorio
                                    val randomProduct = CellstarRepository.products.randomOrNull()
                                    if (randomProduct != null) {
                                        skuInput = randomProduct.sku
                                        Toast.makeText(context, "Escaner: ${randomProduct.name} leído", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear",
                                        tint = primaryColor
                                    )
                                }
                            },
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (skuInput.isNotBlank()) {
                                    val added = CellstarRepository.addProductToCart(skuInput)
                                    if (added) {
                                        skuInput = ""
                                    } else {
                                        Toast.makeText(context, "Producto sin stock o no encontrado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Agregar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Lista de sugerencias de autocompletado
                    if (skuSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column {
                                skuSuggestions.forEach { sugg ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                skuInput = sugg.sku
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${sugg.sku} - ${sugg.name}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Stock: ${sugg.stock}", style = MaterialTheme.typography.bodyMedium, color = primaryColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECCIÓN 2: Carrito Actual
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Carrito Actual",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "$totalCount items",
                        color = primaryColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("El carrito está vacío. Escanee un producto.", color = textSecondary)
                    }
                }
            }
        } else {
            items(cartItems) { cartItem ->
                CartItemCard(cartItem = cartItem)
            }
        }

        // SECCIÓN 3: Datos del Cliente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DATOS DEL CLIENTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                    )

                    Text("DOCUMENTO (ID)", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = clientId,
                        onValueChange = { clientId = it },
                        placeholder = { Text("Ej: 123456789", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("NOMBRE COMPLETO", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        placeholder = { Text("Ej: Juan Pérez", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // SECCIÓN 4: Total & Pagar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL A PAGAR", color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%,.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón gigante PAGAR
                    Button(
                        onClick = {
                            if (cartItems.isEmpty()) {
                                Toast.makeText(context, "Agregue productos antes de pagar", Toast.LENGTH_SHORT).show()
                            } else {
                                // Procesar venta y generar factura
                                val invoiceText = CellstarRepository.checkout(clientName, clientId)

                                // Limpiar datos de cliente local
                                clientId = ""
                                clientName = ""

                                // Lanzar Intent para compartir factura
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, invoiceText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Enviar factura digital vía")
                                context.startActivity(shareIntent)

                                Toast.makeText(context, "Venta procesada con éxito", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "PAGAR",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual para mostrar un producto en el carrito de compras.
 */
@Composable
fun CartItemCard(cartItem: CartItem) {
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val alertColor = Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cartItem.product.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = "SKU: ${cartItem.product.sku}",
                        style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                    )
                }

                Text(
                    text = "$${String.format("%.2f", cartItem.product.price * cartItem.quantity)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alerta de últimas unidades
            if (cartItem.product.stock <= cartItem.product.minStock) {
                Text(
                    text = "⚠️ últimas unidades",
                    color = alertColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Controladores de Cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${cartItem.quantity} x $${String.format("%.2f", cartItem.product.price)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de decrementar
                    IconButton(
                        onClick = { CellstarRepository.decrementCartItem(cartItem.product.sku) },
                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = textPrimary)
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Botón de incrementar
                    IconButton(
                        onClick = { CellstarRepository.incrementCartItem(cartItem.product.sku) },
                        modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = textPrimary)
                    }
                }
            }
        }
    }
}
