package com.example.newattendancetracker.presentation.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newattendancetracker.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                IconButton(onClick = { 
                    Log.d("ProfileScreen", "Back button clicked")
                    onNavigateBack() 
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Header
        ProfileHeader(uiState.user)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile Sections
        ProfileSection(
            title = "Personal Information",
            items = listOf(
                ProfileItem("Email", uiState.user?.email ?: "", Icons.Default.Email),
                ProfileItem("Phone", uiState.user?.phone ?: "", Icons.Default.Phone),
                ProfileItem("Department", uiState.user?.department ?: "", Icons.Default.Business),
                ProfileItem("Position", uiState.user?.position ?: "", Icons.Default.Work),
                ProfileItem("Employee ID", uiState.user?.employeeId ?: "", Icons.Default.Badge)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileSection(
            title = "Work Information",
            items = listOf(
                ProfileItem("Manager", uiState.user?.managerId ?: "N/A", Icons.Default.Person),
                ProfileItem("Join Date", uiState.user?.joinDate?.toString() ?: "", Icons.Default.DateRange),
                ProfileItem("Work Schedule", "${uiState.user?.workStartTime ?: ""} - ${uiState.user?.workEndTime ?: ""}", Icons.Default.Schedule),
                ProfileItem("Location", uiState.user?.workLocation ?: "", Icons.Default.LocationOn)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Settings Section
        SettingsSection(
            onChangePassword = { viewModel.changePassword() },
            onNotificationSettings = { viewModel.openNotificationSettings() },
            onPrivacySettings = { viewModel.openPrivacySettings() },
            onLogout = { viewModel.logout() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Information
        AppInfoSection()
    }
    
    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            user = uiState.user,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                viewModel.updateProfile(updatedUser)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(user: com.example.newattendancetracker.data.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Placeholder
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.name ?: "Loading...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = user?.role?.name ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status Badge
            Surface(
                color = if (user?.isActive == true) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (user?.isActive == true) "Active" else "Inactive",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                items.forEachIndexed { index, item ->
                    ProfileItemRow(item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileItemRow(item: ProfileItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.value.ifEmpty { "Not set" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.value.isEmpty()) 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsSection(
    onChangePassword: () -> Unit,
    onNotificationSettings: () -> Unit,
    onPrivacySettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    subtitle = "Update your password",
                    onClick = onChangePassword
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = onNotificationSettings
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    subtitle = "Privacy settings and security options",
                    onClick = onPrivacySettings
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = onLogout,
                    isDestructive = true
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AppInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Attendance Tracker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "© 2024 Your Company",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: com.example.newattendancetracker.data.model.User?,
    onDismiss: () -> Unit,
    onSave: (com.example.newattendancetracker.data.model.User) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    user?.let { currentUser ->
                        val updatedUser = currentUser.copy(
                            name = name,
                            phone = phone
                        )
                        onSave(updatedUser)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes
data class ProfileItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)