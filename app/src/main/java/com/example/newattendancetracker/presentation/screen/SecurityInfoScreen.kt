package com.example.newattendancetracker.presentation.screen

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityInfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Green
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Your Data is Protected",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Advanced security measures keep your attendance data safe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Security Features
        SecurityFeatureItem(
            icon = Icons.Default.Fingerprint,
            title = "Device Authentication",
            description = "Each device has a unique fingerprint. Records can only be created on authorized devices.",
            color = MaterialTheme.colorScheme.primaryContainer
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecurityFeatureItem(
            icon = Icons.Default.Shield,
            title = "Data Integrity Protection",
            description = "Every attendance record has a unique security signature. Any tampering is immediately detected.",
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecurityFeatureItem(
            icon = Icons.Default.Lock,
            title = "Encrypted Storage",
            description = "Sensitive information is encrypted before being stored on your device.",
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecurityFeatureItem(
            icon = Icons.Default.Visibility,
            title = "Tamper Detection",
            description = "Automatic monitoring detects any unauthorized changes to your attendance records.",
            color = MaterialTheme.colorScheme.errorContainer
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // What This Means for You
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
                    text = "What This Means for You",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                BenefitItem(
                    icon = Icons.Default.CheckCircle,
                    text = "Your attendance records are authentic and trustworthy"
                )
                
                BenefitItem(
                    icon = Icons.Default.Security,
                    text = "Unauthorized modifications are prevented and detected"
                )
                
                BenefitItem(
                    icon = Icons.Default.PrivacyTip,
                    text = "Your personal data remains private and secure"
                )
                
                BenefitItem(
                    icon = Icons.Default.Verified,
                    text = "Managers can trust the accuracy of attendance data"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Technical Details (Collapsible)
        var showTechnicalDetails by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTechnicalDetails = !showTechnicalDetails },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Technical Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Icon(
                        imageVector = if (showTechnicalDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showTechnicalDetails) "Hide details" else "Show details"
                    )
                }
                
                if (showTechnicalDetails) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Security Implementation:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• SHA-256 cryptographic hashing for data integrity\n" +
                               "• AES-256 encryption for sensitive data protection\n" +
                               "• Device-specific fingerprinting using hardware identifiers\n" +
                               "• Real-time tamper detection algorithms\n" +
                               "• Secure local database with Room persistence library\n" +
                               "• No data transmitted over the internet (fully offline)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Green.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your attendance data is secure and protected by multiple layers of security.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SecurityFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BenefitItem(
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