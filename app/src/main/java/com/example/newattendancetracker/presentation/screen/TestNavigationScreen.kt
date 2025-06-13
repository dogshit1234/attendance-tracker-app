package com.example.newattendancetracker.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestNavigationScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    onLogout: () -> Unit
) {
    var clickCount by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Navigation Test Screen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Button clicks: $clickCount",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Test buttons
        Button(
            onClick = { 
                clickCount++
                Log.d("TestNavigationScreen", "Profile button clicked")
                onNavigateToProfile() 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Profile Navigation")
        }
        
        Button(
            onClick = { 
                clickCount++
                Log.d("TestNavigationScreen", "Reports button clicked")
                onNavigateToReports() 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.Assessment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Reports Navigation")
        }
        
        Button(
            onClick = { 
                clickCount++
                Log.d("TestNavigationScreen", "Leaves button clicked")
                onNavigateToLeaves() 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Leaves Navigation")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                clickCount++
                Log.d("TestNavigationScreen", "Logout button clicked")
                onLogout() 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Logout")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🧪 Navigation Test",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Click buttons to test navigation and logout functionality",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}