package com.example.cs_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.cs_manager.ui.theme.CsManagerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CsManagerTheme {
                CsManagerApp()
            }
        }
    }
}

@Composable
fun CsManagerApp() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("home/{username}") { backStackEntry ->

            val username =
                backStackEntry.arguments?.getString("username") ?: ""

            HomeScreen(navController, username)
        }

        composable("inventory") {
            InventoryScreen(navController)
        }

        composable("reports") {
            ReportsScreen(navController)
        }

        composable("alerts") {
            StockAlertScreen(navController)
        }

        composable("sales") {
            SalesScreen(navController)
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    username: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Bienvenido $username",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Panel Administrativo")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("inventory")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Catálogo de productos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("reports")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Reportes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("alerts")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alertas de stock")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("sales")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ventas")
        }
    }
}

@Composable
fun InventoryScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            "Gestión de Inventario",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("• Ver Productos Disponibles")
        Text("• Añadir Nuevos Productos")
        Text("• Actualizar Nivel de Stock")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Atrás")
        }
    }
}

@Composable
fun ReportsScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            "Reportes de Ventas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("• Periodos de alta demanda")
        Text("• Productos más vendidos")
        Text("• Desempeño de ventas")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Atrás")
        }
    }
}

@Composable
fun StockAlertScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            "Alertas de Stock",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("⚠️ Productos Bajos en Stock")
        Text("⚠️ Productos Fuera de Stock")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Atrás")
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Nombre de Usuario")

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contraseña")

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotEmpty()) {

            Text(
                error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (
                    username.isBlank() ||
                    password.isBlank()
                ) {

                    error = "Please fill all fields"

                } else {

                    navController.navigate("home/$username") {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Login")
        }
    }
}

@Composable
fun SalesScreen(navController: NavController) {

    val products = listOf(
        "iPhone 13" to 900.0,
        "Samsung S22" to 850.0,
        "Xiaomi Note 12" to 300.0
    )

    var cart by remember {
        mutableStateOf(
            listOf<Pair<String, Double>>()
        )
    }

    val total = cart.sumOf {
        it.second
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Ventas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Productos Disponibles:")

        products.forEach { product ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    "${product.first} - $${product.second}"
                )

                Button(
                    onClick = {
                        cart = cart + product
                    }
                ) {
                    Text("Agregar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Carrito:")

        cart.forEach {

            Text(
                "• ${it.first} - $${it.second}"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total: $$total")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                cart = emptyList()
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Generar Factura")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Atrás")
        }
    }
}