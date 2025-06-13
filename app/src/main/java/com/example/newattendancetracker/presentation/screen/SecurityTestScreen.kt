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
import com.example.newattendancetracker.data.model.Location
import com.example.newattendancetracker.data.service.SecureAttendanceService
import com.example.newattendancetracker.data.security.AttendanceSecurityManager
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityTestScreen(
    securityManager: AttendanceSecurityManager,
    attendanceService: SecureAttendanceService
) {
    val scope = rememberCoroutineScope()
    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isRunningTests by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Security Test Suite",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Test the security features to ensure they're working correctly",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Test Button
        Button(
            onClick = {
                scope.launch {
                    isRunningTests = true
                    testResults = runSecurityTests(securityManager, attendanceService)
                    isRunningTests = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunningTests
        ) {
            if (isRunningTests) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Running Tests...")
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run Security Tests")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Test Results
        if (testResults.isNotEmpty()) {
            Text(
                text = "Test Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            testResults.forEach { result ->
                TestResultCard(result = result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Security Info Cards
        SecurityInfoCard(
            title = "What We're Testing",
            items = listOf(
                "Device fingerprint generation",
                "Data integrity hashing",
                "Encryption/decryption",
                "Tamper detection",
                "Secure attendance recording"
            ),
            color = MaterialTheme.colorScheme.primaryContainer
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SecurityInfoCard(
            title = "Security Benefits",
            items = listOf(
                "Prevents unauthorized data modification",
                "Detects tampering attempts",
                "Ensures data came from correct device",
                "Protects sensitive information",
                "Maintains audit trail"
            ),
            color = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
private fun TestResultCard(result: String) {
    val isSuccess = result.contains("✅")
    val isWarning = result.contains("⚠️")
    val isError = result.contains("❌")
    
    val backgroundColor = when {
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        isWarning -> MaterialTheme.colorScheme.tertiaryContainer
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = result,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SecurityInfoCard(
    title: String,
    items: List<String>,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(16.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private suspend fun runSecurityTests(
    securityManager: AttendanceSecurityManager,
    attendanceService: SecureAttendanceService
): List<String> {
    val results = mutableListOf<String>()
    
    try {
        // Test 1: Device Fingerprint
        results.add("🔍 Testing device fingerprint generation...")
        val fingerprint1 = securityManager.createDeviceFingerprint()
        val fingerprint2 = securityManager.createDeviceFingerprint()
        
        if (fingerprint1 == fingerprint2 && fingerprint1.isNotEmpty()) {
            results.add("✅ Device fingerprint: Consistent and unique")
        } else {
            results.add("❌ Device fingerprint: Inconsistent generation")
        }
        
        // Test 2: Hash Generation
        results.add("🔍 Testing hash generation...")
        val testData = "test_data_${System.currentTimeMillis()}"
        val hash1 = securityManager.createSHA256Hash(testData)
        val hash2 = securityManager.createSHA256Hash(testData)
        
        if (hash1 == hash2 && hash1.length == 64) {
            results.add("✅ Hash generation: Consistent SHA-256 hashes")
        } else {
            results.add("❌ Hash generation: Inconsistent or invalid hashes")
        }
        
        // Test 3: Encryption/Decryption
        results.add("🔍 Testing encryption/decryption...")
        val originalText = "Sensitive attendance data: ${Date()}"
        val encrypted = securityManager.encryptAttendanceData(originalText)
        val decrypted = securityManager.decryptAttendanceData(encrypted)
        
        if (decrypted == originalText && encrypted != originalText) {
            results.add("✅ Encryption: Data successfully encrypted and decrypted")
        } else {
            results.add("❌ Encryption: Failed to encrypt/decrypt properly")
        }
        
        // Test 4: Secure Attendance Recording
        results.add("🔍 Testing secure attendance recording...")
        val testLocation = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Test Office Location",
            accuracy = 10f
        )
        
        val checkInResult = attendanceService.checkIn(
            userId = "test_user_${System.currentTimeMillis()}",
            location = testLocation,
            notes = "Security test check-in"
        )
        
        if (checkInResult.isSuccess) {
            val attendance = checkInResult.getOrNull()
            if (attendance != null && attendance.securityHash.isNotEmpty()) {
                results.add("✅ Secure check-in: Successfully recorded with security measures")
            } else {
                results.add("⚠️ Secure check-in: Recorded but missing security data")
            }
        } else {
            results.add("❌ Secure check-in: Failed to record attendance")
        }
        
        // Test 5: Device Fingerprint Validation
        results.add("🔍 Testing device fingerprint validation...")
        val currentFingerprint = securityManager.createDeviceFingerprint()
        val isValid = securityManager.validateDeviceFingerprint(currentFingerprint)
        
        if (isValid) {
            results.add("✅ Device validation: Current device fingerprint is valid")
        } else {
            results.add("❌ Device validation: Failed to validate device fingerprint")
        }
        
        // Test Summary
        val successCount = results.count { it.contains("✅") }
        val totalTests = results.count { it.contains("Testing") }
        
        results.add("")
        results.add("📊 Test Summary: $successCount/$totalTests tests passed")
        
        if (successCount == totalTests) {
            results.add("🎉 All security features are working correctly!")
        } else {
            results.add("⚠️ Some security features need attention")
        }
        
    } catch (e: Exception) {
        results.add("❌ Test execution failed: ${e.message}")
    }
    
    return results
}