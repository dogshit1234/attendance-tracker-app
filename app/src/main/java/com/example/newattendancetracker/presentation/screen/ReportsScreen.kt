package com.example.newattendancetracker.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newattendancetracker.presentation.components.AttendanceStatusPieChart
import com.example.newattendancetracker.presentation.components.MonthlyTrendLineChart
import com.example.newattendancetracker.presentation.components.OvertimeAnalysisChart
import com.example.newattendancetracker.presentation.components.WeeklyAttendanceBarChart
import com.example.newattendancetracker.presentation.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Reports & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { viewModel.refreshReports() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Range Selector
        DateRangeSelector(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onDateRangeChanged = { start, end ->
                viewModel.updateDateRange(start, end)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary Cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCardsSection(uiState)
            }
            
            item {
                WeeklyAttendanceBarChart(
                    data = uiState.chartData.map { it.label to it.value }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AttendanceStatusPieChart(
                    presentDays = uiState.presentDays,
                    absentDays = uiState.absentDays,
                    lateDays = uiState.lateDays
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MonthlyTrendLineChart(
                    data = uiState.monthlyTrendData
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OvertimeAnalysisChart(
                    data = uiState.overtimeData
                )
            }
            
            item {
                Text(
                    text = "Detailed Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(uiState.detailedReports) { report ->
                DetailedReportCard(report)
            }
        }
    }
}

@Composable
private fun SummaryCardsSection(uiState: ReportsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Total Hours",
            value = "${uiState.totalHours}h",
            icon = Icons.Default.Schedule,
            color = MaterialTheme.colorScheme.primary
        )
        
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Present Days",
            value = "${uiState.presentDays}",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Overtime",
            value = "${uiState.overtimeHours}h",
            icon = Icons.Default.AccessTime,
            color = Color(0xFFFF9800)
        )
        
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Absent Days",
            value = "${uiState.absentDays}",
            icon = Icons.Default.Cancel,
            color = Color(0xFFF44336)
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AttendanceChart(chartData: List<ChartData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weekly Attendance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart representation
            // TODO: Replace with actual chart library
            chartData.forEach { data ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.label,
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    LinearProgressIndicator(
                        progress = { data.value / 10f }, // Assuming max 10 hours
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                    )
                    
                    Text(
                        text = "${data.value}h",
                        modifier = Modifier.width(40.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedReportCard(report: DetailedReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(report.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusChip(report.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Check In",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = report.checkInTime ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Check Out",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = report.checkOutTime ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Total Hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.totalHours}h",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "present" -> Color(0xFF4CAF50) to Color.White
        "absent" -> Color(0xFFF44336) to Color.White
        "late" -> Color(0xFFFF9800) to Color.White
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelector(
    startDate: Date,
    endDate: Date,
    onDateRangeChanged: (Date, Date) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { /* TODO: Open date picker */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(startDate))
        }
        
        Text(
            text = "to",
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        
        OutlinedButton(
            onClick = { /* TODO: Open date picker */ },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(endDate))
        }
    }
}

// Data classes for the screen
data class ReportsUiState(
    val isLoading: Boolean = false,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val totalHours: Double = 0.0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val overtimeHours: Double = 0.0,
    val chartData: List<ChartData> = emptyList(),
    val monthlyTrendData: List<Pair<String, Float>> = emptyList(),
    val overtimeData: List<Pair<String, Float>> = emptyList(),
    val detailedReports: List<DetailedReport> = emptyList(),
    val error: String? = null
)

data class ChartData(
    val label: String,
    val value: Float
)

data class DetailedReport(
    val date: Date,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val totalHours: Double
)