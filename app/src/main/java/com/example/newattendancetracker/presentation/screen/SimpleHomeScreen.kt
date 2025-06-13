package com.example.newattendancetracker.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    user: User,
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {}
) {
    Log.d("SimpleHomeScreen", "Rendering SimpleHomeScreen for user: ${user.firstName} ${user.lastName}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Attendance Tracker",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Welcome Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome to Attendance Tracker!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Hello, ${user.firstName} ${user.lastName}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // User Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Account Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                InfoRow("Email", user.email)
                InfoRow("Employee ID", user.employeeId)
                InfoRow("Department", user.department)
                InfoRow("Position", user.position)
                InfoRow("Role", user.role.name.lowercase().replaceFirstChar { it.uppercase() })
                
                // Role-specific badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Access Level:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.error
                            UserRole.MANAGER -> MaterialTheme.colorScheme.primary
                            UserRole.EMPLOYEE -> MaterialTheme.colorScheme.secondary
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = when (user.role) {
                                UserRole.ADMIN -> "🔑 Full Access"
                                UserRole.MANAGER -> "👥 Team Management"
                                UserRole.EMPLOYEE -> "👤 Standard Access"
                            },
                            color = when (user.role) {
                                UserRole.ADMIN -> MaterialTheme.colorScheme.onError
                                UserRole.MANAGER -> MaterialTheme.colorScheme.onPrimary
                                UserRole.EMPLOYEE -> MaterialTheme.colorScheme.onSecondary
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Role-based actions
        when (user.role) {
            UserRole.ADMIN -> {
                // Admin gets all features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile")
                    }
                    
                    Button(
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reports")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToLeaves,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leaves")
                    }
                    
                    Button(
                        onClick = { 
                            Log.d("SimpleHomeScreen", "Admin button clicked")
                            onNavigateToAdmin() 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Dashboard button for admin
                Button(
                    onClick = { 
                        Log.d("SimpleHomeScreen", "Dashboard button clicked")
                        onNavigateToDashboard() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dashboard")
                }
            }
            
            UserRole.MANAGER -> {
                // Manager gets team management features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile")
                    }
                    
                    Button(
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reports")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToLeaves,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leaves")
                    }
                    
                    Button(
                        onClick = { 
                            Log.d("SimpleHomeScreen", "Dashboard button clicked")
                            onNavigateToDashboard() 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dashboard")
                    }
                }
            }
            
            UserRole.EMPLOYEE -> {
                // Employee gets basic features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile")
                    }
                    
                    Button(
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reports")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onNavigateToLeaves,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leave Management")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Account Created Successfully!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your attendance tracking journey begins now.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}