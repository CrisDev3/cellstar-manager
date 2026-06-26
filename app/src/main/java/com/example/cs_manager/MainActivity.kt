package com.example.cs_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.navigation.AppNavigation
import com.example.cs_manager.ui.theme.CsManagerTheme

/**
 * Actividad principal de la aplicación.
 * Actúa como punto de entrada del ciclo de vida de la aplicación Android.
 * Inicializa la base de datos Room al inicio para persistencia en ROM.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar repositorio con persistencia a ROM (Room)
        CellstarRepository.initialize(this)

        enableEdgeToEdge()

        setContent {
            CsManagerTheme {
                CsManagerApp()
            }
        }
    }
}

/**
 * Punto de entrada de la interfaz de usuario en Jetpack Compose.
 * Inicializa el NavController y delega el flujo al AppNavigation modularizado.
 */
@Composable
fun CsManagerApp() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}
