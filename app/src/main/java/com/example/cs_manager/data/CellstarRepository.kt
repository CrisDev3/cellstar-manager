package com.example.cs_manager.data

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.room.Room
import com.example.cs_manager.data.local.AppDatabase
import com.example.cs_manager.data.local.dao.LoggedUserPrefDao
import com.example.cs_manager.data.local.dao.MovementLogDao
import com.example.cs_manager.data.local.entity.CartPrefEntity
import com.example.cs_manager.data.local.entity.LoggedUserPrefEntity
import com.example.cs_manager.data.local.entity.MovementLogEntity
import com.example.cs_manager.data.local.entity.ProductEntity
import com.example.cs_manager.data.local.entity.RegisteredUserEntity
import com.example.cs_manager.data.local.entity.SaleTransactionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.set

// ---------------------------------------------------------------------------
// Data classes (in-memory models)
// ---------------------------------------------------------------------------

data class Product(
    val sku: String,
    val name: String,
    val model: String,
    val color: String = "",
    val description: String = "",
    val category: String,
    val price: Double,
    val stock: Int,
    val minStock: Int,
    val imagePath: String? = null
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class MovementLog(
    val dateTime: String,
    val user: String,
    val action: String,
    val actionColor: String
)

data class SaleTransaction(
    val id: String,
    val dateTime: String,
    val clientName: String,
    val clientId: String,
    val items: List<CartItem>,
    val totalAmount: Double
)

data class RegisteredUser(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String
)

// ---------------------------------------------------------------------------
// Repository interface
// ---------------------------------------------------------------------------

interface ICellstarRepository {
    val products: SnapshotStateList<Product>
    val movementLogs: SnapshotStateList<MovementLog>
    val cart: SnapshotStateList<CartItem>
    val salesHistory: SnapshotStateList<SaleTransaction>
    val registeredUsers: SnapshotStateList<RegisteredUser>
    val todaySales: MutableState<Double>
    val weeklyRevenue: MutableState<Double>
    val unitsMoved: MutableState<Int>
    val currentLoggedUser: MutableState<String>
    fun addProduct(product: Product)
    fun registerUser(user: RegisteredUser)
    fun addProductToCart(sku: String): Boolean
    fun incrementCartItem(sku: String)
    fun decrementCartItem(sku: String)
    fun checkout(clientName: String, clientId: String): String
    fun logAction(user: String, action: String, color: String)
}

// ---------------------------------------------------------------------------
// Repository implementation — backed by Room for persistence
// ---------------------------------------------------------------------------

object CellstarRepository : ICellstarRepository {

    // --- Room database reference (initialized from MainActivity) ---
    private var db: AppDatabase? = null
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    /**
     * Call once from MainActivity.onCreate() before setContent.
     * Loads persisted data from Room into the Compose-reactive lists.
     */
    fun initialize(context: Context) {
        if (db != null) return // already initialized
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cellstar_database"
        ).fallbackToDestructiveMigration().build()

        ioScope.launch {
            loadFromDatabase()
        }
    }

    private suspend fun loadFromDatabase() {
        val database = db ?: return

        // Load users
        val users = database.userDao().getAll()
        if (users.isNotEmpty()) {
            registeredUsers.clear()
            registeredUsers.addAll(users.map { it.toRegisteredUser() })
        } else {
            seedDefaultUsers()
        }

        // Load products
        val productEntities = database.productDao().getAll()
        if (productEntities.isNotEmpty()) {
            products.clear()
            products.addAll(productEntities.map { it.toProduct() })
        } else {
            seedDefaultProducts()
        }

        // Load movement logs
        val logs = database.movementLogDao().getAll()
        if (logs.isNotEmpty()) {
            movementLogs.clear()
            movementLogs.addAll(logs.map { it.toMovementLog() })
        } else {
            seedDefaultLogs()
        }

        // Load sales history
        val sales = database.saleTransactionDao().getAll()
        if (sales.isNotEmpty()) {
            salesHistory.clear()
            salesHistory.addAll(sales.map { it.toSaleTransaction() })
            // Recalculate metrics from sales history
            recalculateMetrics()
        }

        // Restore last logged-in user
        val savedPref = database.loggedUserPrefDao().get()
        if (savedPref != null) {
            val userExists = registeredUsers.any {
                it.username.equals(savedPref.username, ignoreCase = true)
            }
            if (userExists) {
                currentLoggedUser.value = savedPref.username
            }
        }

        // Restore saved shopping cart
        val cartPref = database.cartPrefDao().get()
        if (cartPref != null && cartPref.itemsJson.isNotBlank()) {
            val type = object : TypeToken<List<SerializableCartItem>>() {}.type
            val serializedItems: List<SerializableCartItem> = gson.fromJson(cartPref.itemsJson, type)
            if (serializedItems.isNotEmpty()) {
                cart.clear()
                cart.addAll(serializedItems.map { it.toCartItem() })
            }
        }
    }

    // ------------------------------------------------------------------
    // Reactive state (Compose observable)
    // ------------------------------------------------------------------

    override val products = mutableStateListOf<Product>()
    override val movementLogs = mutableStateListOf<MovementLog>()
    override val cart = mutableStateListOf<CartItem>()
    override val salesHistory = mutableStateListOf<SaleTransaction>()
    override val registeredUsers = mutableStateListOf<RegisteredUser>()
    override val todaySales = mutableStateOf(0.0)
    override val weeklyRevenue = mutableStateOf(0.0)
    override val unitsMoved = mutableStateOf(0)
    override val currentLoggedUser = mutableStateOf("Admin")

    // ------------------------------------------------------------------
    // Seed data (only inserted when Room is empty)
    // ------------------------------------------------------------------

    private suspend fun seedDefaultUsers() {
        val defaults = listOf(
            RegisteredUser("System Administrator", "Admin", "admin@cellstar.com", "admin123"),
            RegisteredUser("Carlos Mendoza", "CarlosM", "carlos@cellstar.com", "carlos123"),
            RegisteredUser("David López", "DavidL", "david@cellstar.com", "david123")
        )
        registeredUsers.addAll(defaults)
        db?.userDao()?.insertAll(defaults.map { it.toUserEntity() })
    }

    private suspend fun seedDefaultProducts() {
        val defaults = listOf(
            Product("SKU-9901-XZ", "Quantum Routing Switch", "QR-K700", description = "Switch de enrutamiento cuántico avanzado para redes empresariales de alta fidelidad.", category = "Redes", price = 45.00, stock = 4, minStock = 10),
            Product("SKU-4428-AB", "Optical Transceiver Module", "OTM-10G-LR", description = "Módulo transceptor óptico monomodo 10G LC dúplex de largo alcance.", category = "Redes", price = 18.50, stock = 142, minStock = 20),
            Product("SKU-1105-TR", "Server Rack Enclosure 42U", "SRE-42U-PRO", description = "Gabinete rack para servidores profesionales de 42U con flujo de aire optimizado.", category = "Accesorios", price = 350.00, stock = 18, minStock = 5),
            Product("SKU-7758-PD", "Enterprise Firewall Appliance", "EFA-SEC-V2", description = "Cortafuegos perimetral corporativo para protección contra intrusos y control VPN.", category = "Redes", price = 890.00, stock = 0, minStock = 2),
            Product("SKU-2299-CB", "Cat6A Ethernet Cable Spool", "CAT6A-1000FT", description = "Carrete de cable UTP de cobre macizo Cat6A de 1000 pies con recubrimiento plenum.", category = "Accesorios", price = 125.00, stock = 56, minStock = 15),
            Product("SAM-S23U-256-BLK", "Samsung Galaxy S23 Ultra - 256GB Black", "SM-S918B", color = "Phantom Black", description = "Smartphone insignia con pantalla Dynamic AMOLED de 6.8 y cámara de 200MP.", category = "Celulares", price = 1299.00, stock = 1, minStock = 5),
            Product("ACC-APL-SIL-14PM", "Funda Silicona iPhone 14 Pro Max", "Apple Silicone Case", color = "Azul Tempestad", description = "Funda de silicón oficial de Apple con tecnología MagSafe incorporada.", category = "Accesorios", price = 45.00, stock = 3, minStock = 10),
            Product("CBL-USBC-2M-FAST", "Cable USB-C a USB-C 2M Carga Rápida", "CBL-2M-100W", description = "Cable USB-C trenzado de alta velocidad que soporta suministro de energía de 100W.", category = "Accesorios", price = 12.88, stock = 85, minStock = 100)
        )
        products.addAll(defaults)
        db?.productDao()?.insertAll(defaults.map { it.toProductEntity() })
    }

    private suspend fun seedDefaultLogs() {
        val defaults = listOf(
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
        movementLogs.addAll(defaults)
        db?.movementLogDao()?.insertAll(defaults.map { it.toLogEntity() })
    }

    private fun recalculateMetrics() {
        // Recalculate from persisted sales history
        val sales = salesHistory.toList()
        todaySales.value = 0.0
        weeklyRevenue.value = 0.0
        unitsMoved.value = 0
        if (sales.isNotEmpty()) {
            val now = Calendar.getInstance()
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (tx in sales) {
                val txDate = dateFormat.parse(tx.dateTime) ?: continue
                val txCal = Calendar.getInstance().apply { time = txDate }
                if (txCal.after(todayStart)) todaySales.value += tx.totalAmount
                if (txCal.after(weekStart)) weeklyRevenue.value += tx.totalAmount
                unitsMoved.value += tx.items.sumOf { it.quantity }
            }
        }
    }

    // ------------------------------------------------------------------
    // Business logic
    // ------------------------------------------------------------------

    override fun addProduct(product: Product) {
        products.add(product)
        ioScope.launch { db?.productDao()?.insert(product.toProductEntity()) }
        logAction("System", "ADD PRODUCT - ${product.name} (SKU: ${product.sku})", "blue")
    }

    /**
     * Registers a new user in the in-memory list AND persists to Room.
     */
    override fun registerUser(user: RegisteredUser) {
        registeredUsers.add(user)
        ioScope.launch { db?.userDao()?.insert(user.toUserEntity()) }
        logAction("System", "SIGN UP - @${user.username} registrado con éxito", "blue")
    }

    /**
     * Updates a product's stock in-memory AND persists to Room.
     */
    fun updateProductStock(sku: String, newStock: Int) {
        val index = products.indexOfFirst { it.sku == sku }
        if (index != -1) {
            val updated = products[index].copy(stock = newStock)
            products[index] = updated
            ioScope.launch { db?.productDao()?.insert(updated.toProductEntity()) }
        }
    }

    override fun addProductToCart(sku: String): Boolean {
        val product = products.find { it.sku.equals(sku, ignoreCase = true) } ?: return false
        val existingIndex = cart.indexOfFirst { it.product.sku.equals(sku, ignoreCase = true) }
        if (existingIndex != -1) {
            val item = cart[existingIndex]
            if (item.quantity < product.stock) {
                cart[existingIndex] = item.copy(quantity = item.quantity + 1)
                saveCart()
                return true
            }
            return false
        } else {
            if (product.stock > 0) {
                cart.add(CartItem(product, 1))
                saveCart()
                return true
            }
        }
        return false
    }

    override fun incrementCartItem(sku: String) {
        val index = cart.indexOfFirst { it.product.sku == sku }
        if (index != -1) {
            val item = cart[index]
            val product = products.find { it.sku == sku }
            if (product != null && item.quantity < product.stock) {
                cart[index] = item.copy(quantity = item.quantity + 1)
                saveCart()
            }
        }
    }

    override fun decrementCartItem(sku: String) {
        val index = cart.indexOfFirst { it.product.sku == sku }
        if (index != -1) {
            val item = cart[index]
            if (item.quantity > 1) {
                cart[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cart.removeAt(index)
            }
            saveCart()
        }
    }

    override fun checkout(clientName: String, clientId: String): String {
        if (cart.isEmpty()) return ""

        val totalAmount = cart.sumOf { it.product.price * it.quantity }
        val totalUnits = cart.sumOf { it.quantity }

        // Build invoice text
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

        val itemsCopy = cart.toList()

        // Deduct stock — FIXED: use copy() to trigger SnapshotStateList reactivity
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

            val productIndex = products.indexOfFirst { it.sku == item.product.sku }
            if (productIndex != -1) {
                val original = products[productIndex]
                val newStock = (original.stock - item.quantity).coerceAtLeast(0)
                // Replace the item so SnapshotStateList detects the change
                products[productIndex] = original.copy(stock = newStock)

                // Also persist to Room
                ioScope.launch {
                    db?.productDao()?.insert(products[productIndex].toProductEntity())
                }

                if (newStock < original.minStock) {
                    logAction("System", "CRITICAL STOCK ALERT - ${original.name} ($newStock ud. restantes)", "red")
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

        // Register sale transaction
        val transaction = SaleTransaction(
            id = "TX-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            dateTime = getCurrentDateTime(),
            clientName = if (clientName.isBlank()) "Consumidor Final" else clientName,
            clientId = clientId,
            items = itemsCopy,
            totalAmount = totalAmount
        )
        salesHistory.add(0, transaction)

        // Persist to Room
        ioScope.launch {
            db?.saleTransactionDao()?.insert(transaction.toTransactionEntity())
        }

        // Update metrics
        todaySales.value += totalAmount
        weeklyRevenue.value += totalAmount
        unitsMoved.value += totalUnits

        logAction(
            user = currentLoggedUser.value,
            action = "SALE COMPLETED - Cliente: ${if (clientName.isBlank()) "Consumidor Final" else clientName} ($${String.format("%.2f", totalAmount)})",
            color = "green"
        )

        cart.clear()
        // Clear persisted cart from Room
        ioScope.launch {
            db?.cartPrefDao()?.delete()
        }
        return invoiceBuilder.toString()
    }

    override fun logAction(user: String, action: String, color: String) {
        val log = MovementLog(getCurrentDateTime(), user, action, color)
        movementLogs.add(0, log)
        ioScope.launch { db?.movementLogDao()?.insert(log.toLogEntity()) }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // ------------------------------------------------------------------
    // Mapping extensions (in-memory <-> Room entity)
    // ------------------------------------------------------------------

    private fun Product.toProductEntity() = ProductEntity(
        sku = sku, name = name, model = model, color = color,
        description = description, category = category, price = price,
        stock = stock, minStock = minStock, imagePath = imagePath
    )

    private fun ProductEntity.toProduct() = Product(
        sku = sku, name = name, model = model, color = color,
        description = description, category = category, price = price,
        stock = stock, minStock = minStock, imagePath = imagePath
    )

    private fun RegisteredUser.toUserEntity() = RegisteredUserEntity(
        username = username, fullName = fullName, email = email, password = password
    )

    private fun RegisteredUserEntity.toRegisteredUser() = RegisteredUser(
        fullName = fullName, username = username, email = email, password = password
    )

    private fun MovementLog.toLogEntity() = MovementLogEntity(
        dateTime = dateTime, user = user, action = action, actionColor = actionColor
    )

    private fun MovementLogEntity.toMovementLog() = MovementLog(
        dateTime = dateTime, user = user, action = action, actionColor = actionColor
    )

    private fun SaleTransaction.toTransactionEntity(): SaleTransactionEntity {
        // Serialize CartItem list to JSON using Gson
        val itemsJson = gson.toJson(items.map { SerializableCartItem(it.product.sku, it.product.name, it.product.model, it.product.color, it.product.description, it.product.category, it.product.price, it.product.stock, it.product.minStock, it.product.imagePath, it.quantity) })
        return SaleTransactionEntity(
            id = id, dateTime = dateTime,
            clientName = clientName, clientId = clientId,
            itemsJson = itemsJson, totalAmount = totalAmount
        )
    }

    private fun SaleTransactionEntity.toSaleTransaction(): SaleTransaction {
        val type = object : TypeToken<List<SerializableCartItem>>() {}.type
        val serializedItems: List<SerializableCartItem> = gson.fromJson(itemsJson, type)
        return SaleTransaction(
            id = id, dateTime = dateTime,
            clientName = clientName, clientId = clientId,
            items = serializedItems.map { it.toCartItem() },
            totalAmount = totalAmount
        )
    }

    /**
     * Helper data class for JSON serialization of CartItem.
     */
    private data class SerializableCartItem(
        val sku: String,
        val name: String,
        val model: String,
        val color: String,
        val description: String,
        val category: String,
        val price: Double,
        val stock: Int,
        val minStock: Int,
        val imagePath: String?,
        val quantity: Int
    ) {
        fun toCartItem() = CartItem(
            Product(sku, name, model, color, description, category, price, stock, minStock, imagePath),
            quantity
        )
    }

    /** Helper to insert a list of logs */
    private suspend fun MovementLogDao.insertAll(logs: List<MovementLogEntity>) {
        for (log in logs) insert(log)
    }

    // ------------------------------------------------------------------
    // Cart persistence
    // ------------------------------------------------------------------

    /**
     * Serializes the current cart to JSON and persists it to Room.
     * Captures the cart snapshot on the calling thread to avoid ConcurrentModificationException.
     */
    private fun saveCart() {
        val snapshot = cart.toList() // capture on calling thread (safe)
        ioScope.launch {
            val itemsJson = if (snapshot.isEmpty()) "" else gson.toJson(
                snapshot.map {
                    SerializableCartItem(
                        it.product.sku, it.product.name, it.product.model, it.product.color,
                        it.product.description, it.product.category, it.product.price,
                        it.product.stock, it.product.minStock, it.product.imagePath, it.quantity
                    )
                }
            )
            db?.cartPrefDao()?.insert(CartPrefEntity(itemsJson = itemsJson))
        }
    }

    // ------------------------------------------------------------------
    // Last logged-in user persistence
    // ------------------------------------------------------------------

    /**
     * Saves the given username as the last logged-in user to Room.
     */
    fun saveLastLoggedUser(username: String) {
        currentLoggedUser.value = username
        ioScope.launch {
            db?.loggedUserPrefDao()?.insert(LoggedUserPrefEntity(username = username))
        }
    }

    /**
     * Clears the persisted last logged-in user and resets to empty.
     */
    fun clearLastLoggedUser() {
        currentLoggedUser.value = ""
        ioScope.launch {
            db?.loggedUserPrefDao()?.delete()
        }
    }
}
