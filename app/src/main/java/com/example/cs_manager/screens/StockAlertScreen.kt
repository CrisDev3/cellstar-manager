package com.example.cs_manager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository

/**
 * Pantalla dedicada de Alertas de Stock.
 * Ofrece un listado extendido de productos críticos por debajo de su stock mínimo configurado.
 */
@Composable
fun StockAlertScreen(navController: NavController) {
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val alertColor = Color(0xFFEF4444)
    val successColor = Color(0xFF10B981)
    val primaryColor = Color(0xFF1E3A8A)

    val stockAlerts = CellstarRepository.products.filter { it.stock < it.minStock }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Alertas de Stock Críticas",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        )
        Text(
            "Productos en niveles críticos que requieren reposición inmediata.",
            style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (stockAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("¡Niveles de stock óptimos! ✅", color = successColor, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(stockAlerts) { alertProduct ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alertProduct.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                )
                                Text(
                                    text = "SKU: ${alertProduct.sku} | Mínimo: ${alertProduct.minStock}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${alertProduct.stock} ud.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = alertColor
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = alertColor.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = "Crítico",
                                        color = alertColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Regresar al Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
