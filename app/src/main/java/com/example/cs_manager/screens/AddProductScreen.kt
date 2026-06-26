package com.example.cs_manager.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.data.Product

/**
 * Pantalla de Alta de Producto completa e interactiva.
 * Diseñada al detalle en base a la captura de pantalla de inspiración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var colorVal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var initialStockStr by remember { mutableStateOf("") }
    var minStockStr by remember { mutableStateOf("") }

    // Estados para la carga de imágenes reales
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val bitmapState = remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    bitmapState.value = android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    bitmapState.value = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: android.graphics.Bitmap? ->
        if (bitmap != null) {
            bitmapState.value = bitmap
            // Simular un URI temporal para consistencia
            selectedImageUri = android.net.Uri.parse("camera_temp_image")
        }
    }

    // Categorías y Dropdown
    val categories = listOf("Celulares", "Accesorios", "Redes", "Hardware", "Audio")
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Colores corporativos
    val primaryColor = Color(0xFF1E3A8A)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agregar Producto",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = primaryColor
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Validaciones
                            val price = priceStr.toDoubleOrNull()
                            val stock = initialStockStr.toIntOrNull()
                            val minStock = minStockStr.toIntOrNull()

                            if (name.isBlank() || sku.isBlank() || price == null || stock == null || minStock == null) {
                                Toast.makeText(context, "Por favor complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                            } else if (CellstarRepository.products.any { it.sku.equals(sku, ignoreCase = true) }) {
                                Toast.makeText(context, "Ya existe un producto con el SKU: $sku", Toast.LENGTH_SHORT).show()
                            } else {
                                // Crear y guardar producto
                                val newProduct = Product(
                                    sku = sku,
                                    name = name,
                                    model = model,
                                    color = colorVal,
                                    description = description,
                                    category = selectedCategory,
                                    price = price,
                                    stock = stock,
                                    minStock = minStock,
                                    imagePath = selectedImageUri?.toString()
                                )
                                CellstarRepository.addProduct(newProduct)
                                Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD 1: Subida de Imagen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOTO DEL PRODUCTO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Caja de previsualización para imagen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp))
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = bitmapState.value
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Previsualización de imagen",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tocar para añadir imagen",
                                    color = primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botones de subir imagen / tomar foto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Imagen")
                        }

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tomar Foto", color = Color.White)
                        }
                    }
                }
            }

            // CARD 2: Información Básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre
                    Text("Nombre del Producto *", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Ej: iPhone 15 Pro", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Fila: Modelo y Color
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modelo", fontWeight = FontWeight.Bold, color = textPrimary)
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                placeholder = { Text("Ej: A3106", color = Color.LightGray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Color (Opcional)", fontWeight = FontWeight.Bold, color = textPrimary)
                            OutlinedTextField(
                                value = colorVal,
                                onValueChange = { colorVal = it },
                                placeholder = { Text("Ej: Titanio Natural", color = Color.LightGray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Descripción
                    Text("Descripción", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Detalles del producto, especificaciones...", color = Color.LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // CARD 3: SKU & Categoría
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SKU
                    Text("SKU / Código de Barras *", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = { Text("000000000000", color = Color.LightGray) },
                        trailingIcon = {
                            IconButton(onClick = {
                                // Simular escaneo de código de barras generando un SKU aleatorio
                                val randomSku = "SKU-" + (1000..9999).random() + "-" + ('A'..'Z').random() + ('A'..'Z').random()
                                sku = randomSku
                                Toast.makeText(context, "Escáner: Código leído con éxito", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear",
                                    tint = primaryColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Categoría
                    Text("Categoría", fontWeight = FontWeight.Bold, color = textPrimary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { categoryExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true },
                            shape = RoundedCornerShape(8.dp)
                        )

                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // CARD 4: Precios y Stock
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Precio de Venta
                    Text("Precio de Venta *", fontWeight = FontWeight.Bold, color = textPrimary)
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                priceStr = newValue
                            }
                        },
                        placeholder = { Text("$ 0.00", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Fila: Stock Inicial y Mínimo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stock Inicial *", fontWeight = FontWeight.Bold, color = textPrimary)
                            OutlinedTextField(
                                value = initialStockStr,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        initialStockStr = newValue
                                    }
                                },
                                placeholder = { Text("0", color = Color.LightGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stock Mínimo *", fontWeight = FontWeight.Bold, color = textPrimary)
                            OutlinedTextField(
                                value = minStockStr,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        minStockStr = newValue
                                    }
                                },
                                placeholder = { Text("0", color = Color.LightGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Mensaje de advertencia sutil
                    Text(
                        text = "Alerta de stock bajo automático al caer debajo del mínimo.",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
