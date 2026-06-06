package com.example.cs_manager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.navigation.Routes

/**
 * Pantalla de Login rediseñada con estética premium y moderna.
 * Presenta un contenedor elevado sobre un fondo degradado elegante en tonos azul profundo.
 */
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    // Colores personalizados premium
    val primaryColor = Color(0xFF1E3A8A) // Azul profundo corporativo
    val secondaryColor = Color(0xFF3B82F6) // Azul brillante
    val backgroundColor = Color(0xFFF3F4F6) // Gris muy claro limpio

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.9f), Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logotipo / Icono del Sistema
                Card(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person, // Reemplazo premium para el icono de Cellstar
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cellstar",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                Text(
                    text = "Acceso al Sistema de Inventario",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campo Usuario
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Usuario",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            error = ""
                        },
                        placeholder = { Text("Ingrese su usuario", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Campo Contraseña
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = ""
                        },
                        placeholder = { Text("********", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón Iniciar Sesión
                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            error = "Todos los campos son obligatorios"
                        } else {
                            // Actualizar el usuario activo del repositorio
                            CellstarRepository.currentLoggedUser.value = username
                            // Navegar al Home dinámico
                            navController.navigate(Routes.createHomeRoute(username)) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
