package com.example.cs_manager.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cs_manager.data.CellstarRepository
import com.example.cs_manager.data.MovementLog

/**
 * Pantalla de Reportes e Historial interactiva y premium.
 * Muestra métricas financieras detalladas en tiempo real, ranking de artículos más vendidos
 * y una tabla estilizada con la bitácora histórica de transacciones (Logs).
 */
@Composable
fun ReportsScreen(navController: NavController) {
    val context = LocalContext.current

    // Colores corporativos
    val primaryColor = Color(0xFF1E3A8A)
    val successColor = Color(0xFF10B981)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    // Métricas del repositorio
    val salesToday = CellstarRepository.todaySales.value
    val weeklyRev = CellstarRepository.weeklyRevenue.value
    val unitsMov = CellstarRepository.unitsMoved.value
    val logs = CellstarRepository.movementLogs

    // Calcular productos más vendidos dinámicamente desde el historial
    val dynamicTopProducts = CellstarRepository.salesHistory
        .flatMap { it.items }
        .groupBy { it.product.sku }
        .map { (sku, items) ->
            val product = items.first().product
            val totalQty = items.sumOf { it.quantity }
            Triple(product.name, sku, totalQty)
        }
        .sortedByDescending { it.third }
        .take(5)

    val topProducts = if (dynamicTopProducts.isEmpty()) {
        listOf(
            Triple("Samsung Galaxy S23 Ultra - 256GB Black", "SAM-S23U-256-BLK", 12),
            Triple("Funda Silicona iPhone 14 Pro Max", "ACC-APL-SIL-14PM", 8),
            Triple("Cable USB-C a USB-C 2M Carga Rápida", "CBL-USBC-2M-FAST", 5)
        )
    } else {
        dynamicTopProducts
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

        // ENCABEZADO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reports & History",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = "Key metrics and recent system logs.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                    )
                }

                // Botón Exportar PDF
                Button(
                    onClick = {
                        // Lanzar Intent de compartir texto simulando reporte PDF
                        val reportSummary = StringBuilder().apply {
                            append("REPORTE CELLSTAR MANAGER - RESUMEN DE VENTAS\n")
                            append("============================================\n")
                            append("Ventas de Hoy    : $${String.format("%,.2f", salesToday)}\n")
                            append("Ingresos Semanales: $${String.format("%,.2f", weeklyRev)}\n")
                            append("Unidades Movidas  : $unitsMov unidades\n")
                            append("============================================\n")
                        }.toString()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportSummary)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Exportar reporte analítico vía")
                        context.startActivity(shareIntent)
                        Toast.makeText(context, "Exportando reporte de métricas", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", color = Color.White)
                }
            }
        }

        // METRICAS ANAlÍTICAS DE REPORTES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Métricas 1: Today's Sales
                MetricReportCard(
                    title = "TODAY'S SALES",
                    value = "$${String.format("%,.2f", salesToday)}",
                    badgeText = "+12%",
                    badgeColor = successColor
                )

                // Métricas 2: Weekly Revenue
                MetricReportCard(
                    title = "WEEKLY REVENUE",
                    value = "$${String.format("%,.2f", weeklyRev)}",
                    badgeText = "+4%",
                    badgeColor = successColor
                )

                // Métricas 3: Units Moved (7D)
                MetricReportCard(
                    title = "UNITS MOVED (7D)",
                    value = "$unitsMov",
                    badgeText = "+2%",
                    badgeColor = successColor
                )
            }
        }

        // SECCIÓN: Productos más vendidos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Most Sold Products",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    topProducts.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Nombre y SKU
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.first,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                )
                                Text(
                                    text = "SKU: ${item.second}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Barra de progreso gráfica que representa las ventas
                                val maxSold = topProducts.maxOfOrNull { it.third } ?: 1
                                val fraction = item.third.toFloat() / maxSold.toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction * 0.8f)
                                        .height(6.dp)
                                        .background(primaryColor, shape = RoundedCornerShape(3.dp))
                                )
                            }

                            // Cantidad de Unidades Vendidas
                            Text(
                                text = "${item.third} units",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            )
                        }
                        if (index < topProducts.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }

        // SECCIÓN: Movimientos (Bitácora de Logs)
        item {
            Text(
                text = "Movimientos (Recent Logs)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            )
        }

        itemsIndexed(logs) { _, log ->
            LogMovementRow(log = log)
        }
    }
}

/**
 * Celda individual de estadísticas de reporte analítico.
 */
@Composable
fun MetricReportCard(
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color
) {
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                )
            }

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f))
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Fila estilizada tipo bitácora para mostrar logs de movimientos del sistema.
 */
@Composable
fun LogMovementRow(log: MovementLog) {
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val successColor = Color(0xFF10B981)
    val alertColor = Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.actionColor) {
                            "green" -> successColor
                            "blue" -> Color(0xFF3B82F6)
                            "orange" -> Color(0xFFF59E0B)
                            else -> alertColor
                        }
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Usuario: ${log.user}",
                        style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                    )
                    Text(
                        text = log.dateTime,
                        style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                    )
                }
            }
        }
    }
}
