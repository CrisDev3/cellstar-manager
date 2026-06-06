package com.example.cs_manager.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa un producto en el inventario.
 */
data class Product(
    val sku: String,
    val name: String,
    val model: String,
    val color: String = "",
    val description: String = "",
    val category: String,
    val price: Double,
    var stock: Int,
    val minStock: Int
)

/**
 * Representa un artículo en el carrito de compras.
 */
data class CartItem(
    val product: Product,
    var quantity: Int
)

/**
 * Representa un registro de log o movimiento de auditoría.
 */
data class MovementLog(
    val dateTime: String,
    val user: String,
    val action: String,
    val actionColor: String // "green", "blue", "orange", "red"
)

/**
 * Repositorio centralizado con estados reactivos de Jetpack Compose.
 * Actúa como base de datos en memoria para simular flujos interactivos de negocio en tiempo real.
 */
object CellstarRepository {

    // Lista reactiva de productos del inventario
    val products = mutableStateListOf<Product>()

    // Lista reactiva del historial de movimientos (logs)
    val movementLogs = mutableStateListOf<MovementLog>()

    // Estado reactivo del carrito de compras
    val cart = mutableStateListOf<CartItem>()

    // Métricas globales reactivas
    val todaySales = mutableStateOf(4250.00)
    val weeklyRevenue = mutableStateOf(28140.50)
    val unitsMoved = mutableStateOf(1402)
    val currentLoggedUser = mutableStateOf("Admin")

    init {
        // Cargar productos de demostración basados en las capturas de pantalla
        products.addAll(
            listOf(
                Product(
                    sku = "SKU-9901-XZ",
                    name = "Quantum Routing Switch",
                    model = "QR-K700",
                    description = "Switch de enrutamiento cuántico avanzado para redes empresariales de alta fidelidad.",
                    category = "Redes",
                    price = 45.00,
                    stock = 4,
                    minStock = 10
                ),
                Product(
                    sku = "SKU-4428-AB",
                    name = "Optical Transceiver Module",
                    model = "OTM-10G-LR",
                    description = "Módulo transceptor óptico monomodo 10G LC dúplex de largo alcance.",
                    category = "Redes",
                    price = 18.50,
                    stock = 142,
                    minStock = 20
                ),
                Product(
                    sku = "SKU-1105-TR",
                    name = "Server Rack Enclosure 42U",
                    model = "SRE-42U-PRO",
                    description = "Gabinete rack para servidores profesionales de 42U con flujo de aire optimizado.",
                    category = "Accesorios",
                    price = 350.00,
                    stock = 18,
                    minStock = 5
                ),
                Product(
                    sku = "SKU-7758-PD",
                    name = "Enterprise Firewall Appliance",
                    model = "EFA-SEC-V2",
                    description = "Cortafuegos perimetral corporativo para protección contra intrusos y control VPN.",
                    category = "Redes",
                    price = 890.00,
                    stock = 0,
                    minStock = 2
                ),
                Product(
                    sku = "SKU-2299-CB",
                    name = "Cat6A Ethernet Cable Spool",
                    model = "CAT6A-1000FT",
                    description = "Carrete de cable UTP de cobre macizo Cat6A de 1000 pies con recubrimiento plenum.",
                    category = "Accesorios",
                    price = 125.00,
                    stock = 56,
                    minStock = 15
                ),
                Product(
                    sku = "SAM-S23U-256-BLK",
                    name = "Samsung Galaxy S23 Ultra - 256GB Black",
                    model = "SM-S918B",
                    color = "Phamtom Black",
                    description = "Smartphone insignia con pantalla Dynamic AMOLED de 6.8 y cámara de 200MP.",
                    category = "Celulares",
                    price = 1299.00,
                    stock = 1,
                    minStock = 5
                ),
                Product(
                    sku = "ACC-APL-SIL-14PM",
                    name = "Funda Silicona iPhone 14 Pro Max",
                    model = "Apple Silicone Case",
                    color = "Azul Tempestad",
                    description = "Funda de silicón oficial de Apple con tecnología MagSafe incorporada.",
                    category = "Accesorios",
                    price = 45.00,
                    stock = 3,
                    minStock = 10
                ),
                Product(
                    sku = "CBL-USBC-2M-FAST",
                    name = "Cable USB-C a USB-C 2M Carga Rápida",
                    model = "CBL-2M-100W",
                    description = "Cable USB-C trenzado de alta velocidad que soporta suministro de energía de 100W.",
                    category = "Accesorios",
                    price = 12.88,
                    stock = 85,
                    minStock = 100 // Puesto alto para simular alerta crítica
                )
            )
        )

        // Cargar logs iniciales del historial
        movementLogs.addAll(
            listOf(
                MovementLog("2026-05-27 19:30", "Carlos M.", "SALE COMPLETED - Ticket #8492 ($1,299.00)", "green"),
                MovementLog("2026-05-27 18:45", "System", "STOCK UPDATE - Mercancía Proveedor X (+50)", "blue"),
                MovementLog("2026-05-27 16:15", "David L.", "PRICE ADJUST - SKU-9901-XZ", "orange"),
                MovementLog("2026-05-27 14:32", "Maria G.", "SALE COMPLETED", "green"),
                MovementLog("2026-05-27 13:15", "System", "STOCK UPDATE", "blue"),
                MovementLog("2026-05-27 11:40", "David L.", "PRICE ADJUST", "orange"),
                MovementLog("2026-05-27 09:05", "Maria G.", "REPORT EXPORT", "blue"),
                MovementLog("2026-05-26 16:55", "System", "LOW STOCK ALERT", "red"),
                MovementLog("2026-05-26 14:20", "David L.", "SALE COMPLETED", "green")
            )
        )
    }

    /**
     * Añade un nuevo producto al catálogo.
     */
    fun addProduct(product: Product) {
        products.add(product)
        logAction("System", "ADD PRODUCT - ${product.name} (SKU: ${product.sku})", "blue")
    }

    /**
     * Agrega un producto al carrito de ventas mediante su SKU.
     */
    fun addProductToCart(sku: String): Boolean {
        val product = products.find { it.sku.equals(sku, ignoreCase = true) } ?: return false
        
        // Comprobar si el producto ya existe en el carrito
        val existingIndex = cart.indexOfFirst { it.product.sku.equals(sku, ignoreCase = true) }
        if (existingIndex != -1) {
            val item = cart[existingIndex]
            if (item.quantity < product.stock) {
                // Actualizar de forma reactiva recreando la referencia
                cart[existingIndex] = item.copy(quantity = item.quantity + 1)
                return true
            }
            return false
        } else {
            if (product.stock > 0) {
                cart.add(CartItem(product, 1))
                return true
            }
        }
        return false
    }

    /**
     * Incrementa la cantidad de un artículo en el carrito.
     */
    fun incrementCartItem(sku: String) {
        val index = cart.indexOfFirst { it.product.sku == sku }
        if (index != -1) {
            val item = cart[index]
            val product = products.find { it.sku == sku }
            if (product != null && item.quantity < product.stock) {
                cart[index] = item.copy(quantity = item.quantity + 1)
            }
        }
    }

    /**
     * Decrementa la cantidad de un artículo en el carrito o lo remueve si llega a 0.
     */
    fun decrementCartItem(sku: String) {
        val index = cart.indexOfFirst { it.product.sku == sku }
        if (index != -1) {
            val item = cart[index]
            if (item.quantity > 1) {
                cart[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cart.removeAt(index)
            }
        }
    }

    /**
     * Procesa la compra del carrito actual.
     * Descuenta existencias, incrementa métricas financieras, genera alertas si es necesario,
     * registra el log de venta y limpia el carrito.
     * @return El texto estructurado de la factura de venta para compartir.
     */
    fun checkout(clientName: String, clientId: String): String {
        if (cart.isEmpty()) return ""

        val totalAmount = cart.sumOf { it.product.price * it.quantity }
        val totalUnits = cart.sumOf { it.quantity }

        val invoiceBuilder = StringBuilder().apply {
            append("===================================\n")
            append("       FACTURA CELLSTAR MANAGER    \n")
            append("===================================\n")
            append("Fecha/Hora : ${getCurrentDateTime()}\n")
            append("Atendido por: ${currentLoggedUser.value}\n")
            if (clientName.isNotBlank()) {
                append("Cliente    : $clientName\n")
                append("Identif. ID: $clientId\n")
            }
            append("-----------------------------------\n")
            append(String.format("%-18s %3s %9s\n", "Producto", "Cant", "Subtotal"))
            append("-----------------------------------\n")
        }

        cart.forEach { item ->
            val subtotal = item.product.price * item.quantity
            invoiceBuilder.append(
                String.format(
                    "%-18s %3d $%9.2f\n",
                    if (item.product.name.length > 18) item.product.name.substring(0, 15) + "..." else item.product.name,
                    item.quantity,
                    subtotal
                )
            )

            // Descontar stock real en el catálogo
            val originalProduct = products.find { it.sku == item.product.sku }
            if (originalProduct != null) {
                val previousStock = originalProduct.stock
                originalProduct.stock = (previousStock - item.quantity).coerceAtLeast(0)
                
                // Si el stock cae por debajo del mínimo, verificar alertas críticas
                if (originalProduct.stock < originalProduct.minStock) {
                    logAction("System", "CRITICAL STOCK ALERT - ${originalProduct.name} (${originalProduct.stock} ud. restantes)", "red")
                }
            }
        }

        invoiceBuilder.apply {
            append("-----------------------------------\n")
            append(String.format("TOTAL NETO :                     $%9.2f\n", totalAmount))
            append("===================================\n")
            append("    ¡Gracias por comprar en Cellstar!  \n")
            append("===================================\n")
        }

        // Incrementar estadísticas financieras
        todaySales.value += totalAmount
        weeklyRevenue.value += totalAmount
        unitsMoved.value += totalUnits

        // Registrar auditoría de venta
        logAction(
            user = currentLoggedUser.value,
            action = "SALE COMPLETED - Cliente: ${if (clientName.isBlank()) "Consumidor Final" else clientName} ($${String.format("%.2f", totalAmount)})",
            color = "green"
        )

        // Limpiar el carrito de compras
        cart.clear()

        return invoiceBuilder.toString()
    }

    /**
     * Añade un registro al historial de logs.
     */
    fun logAction(user: String, action: String, color: String) {
        movementLogs.add(0, MovementLog(getCurrentDateTime(), user, action, color))
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
