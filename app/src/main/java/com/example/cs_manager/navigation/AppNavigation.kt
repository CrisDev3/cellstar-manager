package com.example.cs_manager.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.screens.*
import kotlinx.coroutines.launch

/**
 * Host de navegación y Scaffold general de la aplicación.
 * Implementa una barra superior ("Admin Suite") y una barra inferior de navegación unificada
 * (Dashboard, Inventario, Ventas, Reportes) con una interfaz premium y moderna.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    // Escuchar el estado de la ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determinar si debemos mostrar las barras de navegación
    val showBars = currentRoute != null &&
                   currentRoute != Routes.LOGIN &&
                   currentRoute != Routes.SIGNUP &&
                   currentRoute != Routes.ADD_PRODUCT

    // Colores corporativos
    val primaryColor = Color(0xFF1E3A8A)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    // Estado del Drawer lateral de perfil
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Obtener la información del usuario logueado desde el repositorio
    val currentUsername = CellstarRepository.currentLoggedUser.value
    val currentUser = CellstarRepository.registeredUsers.find {
        it.username.equals(currentUsername, ignoreCase = true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBars,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // Header con gradiente del drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(primaryColor, Color(0xFF3B82F6))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        // Avatar del usuario
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentUser?.fullName ?: currentUsername,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "@$currentUsername",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Información del usuario
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Fila correo
                    if (currentUser != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Correo electrónico",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = currentUser.email,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fila rol (siempre visible)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rol",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Administrador",
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón Cerrar Sesión
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() }
                            CellstarRepository.clearLastLoggedUser()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar Sesión",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) {
    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Admin Suite",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    actions = {
                        // Campana de Notificaciones
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Perfil de Usuario — abre el Drawer lateral
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.1f))
                                .clickable {
                                    scope.launch { drawerState.open() }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Abrir perfil",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            if (showBars) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    // Item 1: Dashboard
                    NavigationBarItem(
                        selected = currentRoute?.startsWith("home") == true,
                        onClick = {
                            val user = CellstarRepository.currentLoggedUser.value
                            navController.navigate(Routes.createHomeRoute(user)) {
                                popUpTo(Routes.createHomeRoute(user)) { inclusive = true }
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.1f),
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary
                        )
                    )

                    // Item 2: Inventario
                    NavigationBarItem(
                        selected = currentRoute == Routes.INVENTORY,
                        onClick = {
                            navController.navigate(Routes.INVENTORY) {
                                popUpTo(Routes.INVENTORY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Inventario") },
                        label = { Text("Inventario") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.1f),
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary
                        )
                    )

                    // Item 3: Ventas
                    NavigationBarItem(
                        selected = currentRoute == Routes.SALES,
                        onClick = {
                            navController.navigate(Routes.SALES) {
                                popUpTo(Routes.SALES) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Ventas") },
                        label = { Text("Ventas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.1f),
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary
                        )
                    )

                    // Item 4: Reportes
                    NavigationBarItem(
                        selected = currentRoute == Routes.REPORTS,
                        onClick = {
                            navController.navigate(Routes.REPORTS) {
                                popUpTo(Routes.REPORTS) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Reportes") },
                        label = { Text("Reportes") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor.copy(alpha = 0.1f),
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(navController)
            }

            composable(Routes.SIGNUP) {
                SignUpScreen(navController)
            }

            composable(Routes.HOME) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                HomeScreen(navController, username)
            }

            composable(Routes.INVENTORY) {
                InventoryScreen(navController)
            }

            composable(Routes.REPORTS) {
                ReportsScreen(navController)
            }

            composable(Routes.ALERTS) {
                StockAlertScreen(navController)
            }

            composable(Routes.SALES) {
                SalesScreen(navController)
            }

            composable(Routes.ADD_PRODUCT) {
                AddProductScreen(navController)
            }
        }
    }
    } // Cierra ModalNavigationDrawer
}
