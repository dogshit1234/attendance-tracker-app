package com.example.newattendancetracker.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.presentation.viewmodel.AttendanceState
import com.example.newattendancetracker.presentation.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    attendanceViewModel: AttendanceViewModel = hiltViewModel()
) {
    val attendanceState by attendanceViewModel.attendanceState.collectAsState()
    val todayAttendance by attendanceViewModel.todayAttendance.collectAsState()
    val monthlyAttendance by attendanceViewModel.monthlyAttendance.collectAsState()
    val isOnBreak by attendanceViewModel.isOnBreak.collectAsState()
    
    LaunchedEffect(user.id) {
        attendanceViewModel.loadTodayAttendance(user.id)
        attendanceViewModel.loadMonthlyAttendance(user.id)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${user.firstName}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Quick Actions
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Attendance Card
            item {
                AttendanceCard(
                    attendanceState = attendanceState,
                    todayAttendance = todayAttendance,
                    isOnBreak = isOnBreak,
                    onCheckIn = { attendanceViewModel.checkIn(user.id) },
                    onCheckOut = { attendanceViewModel.checkOut(user.id) },
                    onStartBreak = { attendanceViewModel.startBreak(user.id) },
                    onEndBreak = { attendanceViewModel.endBreak(user.id) }
                )
            }
            
            // Monthly Stats Card
            item {
                MonthlyStatsCard(
                    stats = attendanceViewModel.getMonthlyStats(user.id)
                )
            }
            
            // Quick Actions
            item {
                QuickActionsCard(
                    onNavigateToReports = onNavigateToReports,
                    onNavigateToLeaves = onNavigateToLeaves
                )
            }
            
            // Recent Attendance
            item {
                RecentAttendanceCard(
                    attendances = monthlyAttendance.take(5)
                )
            }
        }
    }
}

@Composable
fun AttendanceCard(
    attendanceState: AttendanceState,
    todayAttendance: com.example.newattendancetracker.data.model.Attendance?,
    isOnBreak: Boolean,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onStartBreak: () -> Unit,
    onEndBreak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Attendance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when (attendanceState) {
                is AttendanceState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is AttendanceState.NotCheckedIn -> {
                    Button(
                        onClick = onCheckIn,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check In")
                    }
                }
                
                else -> {
                    todayAttendance?.let { attendance ->
                        // Check-in time
                        attendance.checkInTime?.let { checkInTime ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Check-in:")
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkInTime),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Check-out time
                        attendance.checkOutTime?.let { checkOutTime ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Check-out:")
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkOutTime),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Working hours
                        if (attendance.totalHours > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Hours worked:")
                                Text(
                                    String.format("%.1f hrs", attendance.totalHours),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (attendance.checkOutTime == null) {
                                // Break button
                                if (isOnBreak) {
                                    Button(
                                        onClick = onEndBreak,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("End Break")
                                    }
                                } else {
                                    Button(
                                        onClick = onStartBreak,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Break")
                                    }
                                }
                                
                                // Check-out button
                                Button(
                                    onClick = onCheckOut,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check Out")
                                }
                            } else {
                                Text(
                                    text = "You have checked out for today",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Error message
            if (attendanceState is AttendanceState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = attendanceState.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MonthlyStatsCard(
    stats: com.example.newattendancetracker.presentation.viewmodel.MonthlyStats
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "This Month",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Present",
                    value = "${stats.presentDays}",
                    subtitle = "days"
                )
                StatItem(
                    title = "Hours",
                    value = String.format("%.1f", stats.totalHours),
                    subtitle = "total"
                )
                StatItem(
                    title = "Overtime",
                    value = String.format("%.1f", stats.overtimeHours),
                    subtitle = "hours"
                )
                StatItem(
                    title = "Late",
                    value = "${stats.lateDays}",
                    subtitle = "days"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsCard(
    onNavigateToReports: () -> Unit,
    onNavigateToLeaves: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reports")
                }
                
                OutlinedButton(
                    onClick = onNavigateToLeaves,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leaves")
                }
            }
        }
    }
}

@Composable
fun RecentAttendanceCard(
    attendances: List<com.example.newattendancetracker.data.model.Attendance>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Recent Attendance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (attendances.isEmpty()) {
                Text(
                    text = "No attendance records found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                attendances.forEach { attendance ->
                    AttendanceItem(attendance = attendance)
                    if (attendance != attendances.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(
    attendance: com.example.newattendancetracker.data.model.Attendance
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(attendance.date),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = attendance.status.name.replace("_", " "),
                fontSize = 12.sp,
                color = when (attendance.status) {
                    com.example.newattendancetracker.data.model.AttendanceStatus.PRESENT -> Color.Green
                    com.example.newattendancetracker.data.model.AttendanceStatus.LATE -> Color(0xFFFF9800)
                    com.example.newattendancetracker.data.model.AttendanceStatus.ABSENT -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            attendance.checkInTime?.let { checkIn ->
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkIn),
                    fontWeight = FontWeight.Medium
                )
            }
            if (attendance.totalHours > 0) {
                Text(
                    text = String.format("%.1f hrs", attendance.totalHours),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}