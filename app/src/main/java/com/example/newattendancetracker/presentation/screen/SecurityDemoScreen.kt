package com.example.newattendancetracker.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newattendancetracker.data.security.AttendanceSecurityManager
import com.example.newattendancetracker.presentation.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDemoScreen(
    securityManager: AttendanceSecurityManager,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    var deviceFingerprint by remember { mutableStateOf("") }
    var sampleHash by remember { mutableStateOf("") }
    var encryptedText by remember { mutableStateOf("") }
    var decryptedText by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        deviceFingerprint = securityManager.createDeviceFingerprint()
        sampleHash = securityManager.createSHA256Hash("Sample attendance data")
        
        val originalText = "Check-in: 09:00 AM, Location: Office"
        encryptedText = securityManager.encryptAttendanceData(originalText)
        decryptedText = securityManager.decryptAttendanceData(encryptedText)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Security Features Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "This demonstrates the security measures protecting your attendance data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Security Features Cards
        SecurityFeatureCard(
            title = "Device Fingerprinting",
            description = "Each device has a unique fingerprint to detect data transfers",
            icon = Icons.Default.Fingerprint,
            details = "Your device fingerprint: ${deviceFingerprint.take(16)}...",
            color = MaterialTheme.colorScheme.primaryContainer
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SecurityFeatureCard(
            title = "Data Integrity Hashing",
            description = "Each attendance record has a unique hash to detect tampering",
            icon = Icons.Default.Security,
            details = "Sample hash: ${sampleHash.take(16)}...",
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SecurityFeatureCard(
            title = "Data Encryption",
            description = "Sensitive data is encrypted before storage",
            icon = Icons.Default.Lock,
            details = "Encrypted: ${encryptedText.take(20)}...\nDecrypted: $decryptedText",
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SecurityFeatureCard(
            title = "Tamper Detection",
            description = "Automatic detection of modified attendance records",
            icon = Icons.Default.Warning,
            details = "Records are continuously monitored for unauthorized changes",
            color = MaterialTheme.colorScheme.errorContainer
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Security Benefits
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Security Benefits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SecurityBenefitItem(
                    icon = Icons.Default.Shield,
                    text = "Prevents unauthorized modification of attendance records"
                )
                
                SecurityBenefitItem(
                    icon = Icons.Default.Visibility,
                    text = "Detects when data has been tampered with"
                )
                
                SecurityBenefitItem(
                    icon = Icons.Default.PhoneAndroid,
                    text = "Identifies records created on different devices"
                )
                
                SecurityBenefitItem(
                    icon = Icons.Default.Lock,
                    text = "Encrypts sensitive information"
                )
                
                SecurityBenefitItem(
                    icon = Icons.Default.AdminPanelSettings,
                    text = "Provides security dashboard for administrators"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Technical Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Technical Implementation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• SHA-256 hashing for data integrity\n" +
                           "• AES encryption for sensitive data\n" +
                           "• Device-specific fingerprinting\n" +
                           "• Real-time tamper detection\n" +
                           "• Secure local storage with Room database",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SecurityFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityBenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Green
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}