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
import com.example.newattendancetracker.data.model.LeaveStatus
import com.example.newattendancetracker.data.model.LeaveType
import com.example.newattendancetracker.presentation.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LeaveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddLeaveDialog by remember { mutableStateOf(false) }
    
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
                    text = "Leave Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            FloatingActionButton(
                onClick = { showAddLeaveDialog = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apply for Leave")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Leave Balance Cards
        LeaveBalanceSection(uiState.leaveBalance)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Tabs
        LeaveFilterTabs(
            selectedFilter = uiState.selectedFilter,
            onFilterChanged = viewModel::updateFilter
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Leave Requests List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredLeaves) { leave ->
                    LeaveRequestCard(
                        leave = leave,
                        onApprove = { viewModel.approveLeave(leave.id) },
                        onReject = { viewModel.rejectLeave(leave.id) },
                        onCancel = { viewModel.cancelLeave(leave.id) },
                        showActions = uiState.canManageLeaves
                    )
                }
            }
        }
    }
    
    // Add Leave Dialog
    if (showAddLeaveDialog) {
        AddLeaveDialog(
            onDismiss = { showAddLeaveDialog = false },
            onSubmit = { leaveRequest ->
                viewModel.submitLeaveRequest(leaveRequest)
                showAddLeaveDialog = false
            }
        )
    }
}

@Composable
private fun LeaveBalanceSection(leaveBalance: Map<LeaveType, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Leave Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                leaveBalance.forEach { (type, balance) ->
                    LeaveBalanceItem(
                        type = type.name,
                        balance = balance,
                        color = getLeaveTypeColor(type)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaveBalanceItem(
    type: String,
    balance: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$balance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = type,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LeaveFilterTabs(
    selectedFilter: LeaveFilter,
    onFilterChanged: (LeaveFilter) -> Unit
) {
    val filters = LeaveFilter.values()
    
    ScrollableTabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterChanged(filter) },
                text = { Text(filter.displayName) }
            )
        }
    }
}

@Composable
private fun LeaveRequestCard(
    leave: LeaveRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    showActions: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = leave.type.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatDate(leave.startDate)} - ${formatDate(leave.endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                LeaveStatusChip(leave.status)
            }
            
            if (leave.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason: ${leave.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: ${leave.duration} day(s)",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "Applied: ${formatDate(leave.appliedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons for managers
            if (showActions && leave.status == LeaveStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onReject) {
                        Text("Reject", color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onApprove) {
                        Text("Approve")
                    }
                }
            }
            
            // Cancel button for own requests
            if (!showActions && leave.status == LeaveStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancel Request")
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaveStatusChip(status: LeaveStatus) {
    val (backgroundColor, textColor) = when (status) {
        LeaveStatus.PENDING -> Color(0xFFFF9800) to Color.White
        LeaveStatus.APPROVED -> Color(0xFF4CAF50) to Color.White
        LeaveStatus.REJECTED -> Color(0xFFF44336) to Color.White
        LeaveStatus.CANCELLED -> Color(0xFF9E9E9E) to Color.White
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status.name,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLeaveDialog(
    onDismiss: () -> Unit,
    onSubmit: (LeaveRequest) -> Unit
) {
    var selectedType by remember { mutableStateOf(LeaveType.ANNUAL) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Leave") },
        text = {
            Column {
                // Leave Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Leave Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date pickers (simplified)
                OutlinedTextField(
                    value = formatDate(startDate),
                    onValueChange = { },
                    label = { Text("Start Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = formatDate(endDate),
                    onValueChange = { },
                    label = { Text("End Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val leaveRequest = LeaveRequest(
                        id = UUID.randomUUID().toString(),
                        userId = "current_user", // TODO: Get from auth
                        type = selectedType,
                        startDate = startDate,
                        endDate = endDate,
                        reason = reason,
                        status = LeaveStatus.PENDING,
                        appliedDate = Date(),
                        duration = calculateDuration(startDate, endDate)
                    )
                    onSubmit(leaveRequest)
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getLeaveTypeColor(type: LeaveType): Color {
    return when (type) {
        LeaveType.ANNUAL -> Color(0xFF2196F3)
        LeaveType.SICK -> Color(0xFFF44336)
        LeaveType.PERSONAL -> Color(0xFF4CAF50)
        LeaveType.MATERNITY -> Color(0xFFE91E63)
        LeaveType.PATERNITY -> Color(0xFF9C27B0)
        LeaveType.EMERGENCY -> Color(0xFFFF5722)
        LeaveType.CASUAL -> Color(0xFF00BCD4)
        LeaveType.UNPAID -> Color(0xFF795548)
        LeaveType.COMPENSATORY -> Color(0xFF607D8B)
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
}

private fun calculateDuration(startDate: Date, endDate: Date): Int {
    val diffInMillis = endDate.time - startDate.time
    return (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
}

// Data classes and enums
enum class LeaveFilter(val displayName: String) {
    ALL("All"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

data class LeaveRequest(
    val id: String,
    val userId: String,
    val type: LeaveType,
    val startDate: Date,
    val endDate: Date,
    val reason: String,
    val status: LeaveStatus,
    val appliedDate: Date,
    val duration: Int
)