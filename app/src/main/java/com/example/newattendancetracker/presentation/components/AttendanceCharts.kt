package com.example.newattendancetracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun WeeklyAttendanceBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Attendance Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isNotEmpty()) {
                SimpleBarChart(
                    data = data,
                    modifier = Modifier.height(300.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendLineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Attendance Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isNotEmpty()) {
                SimpleLineChart(
                    data = data,
                    modifier = Modifier.height(300.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusPieChart(
    presentDays: Int,
    absentDays: Int,
    lateDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Attendance Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val total = presentDays + absentDays + lateDays
            
            if (total > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimplePieChart(
                        presentDays = presentDays,
                        absentDays = absentDays,
                        lateDays = lateDays,
                        modifier = Modifier.size(200.dp)
                    )
                    
                    Column {
                        LegendItem("Present", presentDays, Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(8.dp))
                        LegendItem("Absent", absentDays, Color(0xFFF44336))
                        Spacer(modifier = Modifier.height(8.dp))
                        LegendItem("Late", lateDays, Color(0xFFFF9800))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun OvertimeAnalysisChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overtime Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isNotEmpty()) {
                SimpleBarChart(
                    data = data,
                    modifier = Modifier.height(250.dp),
                    barColor = Color(0xFFFF9800)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No overtime data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Simple chart implementations using Canvas
@Composable
private fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    
    Canvas(modifier = modifier.fillMaxWidth()) {
        val barWidth = size.width / data.size * 0.7f
        val spacing = size.width / data.size * 0.3f
        
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / maxValue) * size.height * 0.8f
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    
    Canvas(modifier = modifier.fillMaxWidth()) {
        if (data.size > 1) {
            val points = data.mapIndexed { index, (_, value) ->
                val x = (index.toFloat() / (data.size - 1)) * size.width
                val y = size.height - (value / maxValue) * size.height * 0.8f
                Offset(x, y)
            }
            
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f
                )
            }
            
            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 6f,
                    center = point
                )
            }
        }
    }
}

@Composable
private fun SimplePieChart(
    presentDays: Int,
    absentDays: Int,
    lateDays: Int,
    modifier: Modifier = Modifier
) {
    val total = presentDays + absentDays + lateDays
    if (total == 0) return
    
    val presentAngle = (presentDays.toFloat() / total) * 360f
    val absentAngle = (absentDays.toFloat() / total) * 360f
    val lateAngle = (lateDays.toFloat() / total) * 360f
    
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2 * 0.8f
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = 0f
        
        // Present slice
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = startAngle,
            sweepAngle = presentAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += presentAngle
        
        // Absent slice
        drawArc(
            color = Color(0xFFF44336),
            startAngle = startAngle,
            sweepAngle = absentAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += absentAngle
        
        // Late slice
        drawArc(
            color = Color(0xFFFF9800),
            startAngle = startAngle,
            sweepAngle = lateAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}