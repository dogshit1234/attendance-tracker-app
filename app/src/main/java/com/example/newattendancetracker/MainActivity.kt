package com.example.newattendancetracker

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newattendancetracker.presentation.screen.HomeScreen
import com.example.newattendancetracker.presentation.screen.LeaveManagementScreen
import com.example.newattendancetracker.presentation.screen.LoginScreen
import com.example.newattendancetracker.presentation.screen.ProfileScreen
import com.example.newattendancetracker.presentation.screen.ReportsScreen
import com.example.newattendancetracker.presentation.screen.SignUpScreen
import com.example.newattendancetracker.presentation.screen.SimpleHomeScreen
import com.example.newattendancetracker.presentation.screen.TestNavigationScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newattendancetracker.presentation.viewmodel.AuthState
import com.example.newattendancetracker.presentation.viewmodel.AuthViewModel
import com.example.newattendancetracker.ui.theme.NewAttendanceTrackerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewAttendanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AttendanceTrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AttendanceTrackerApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    // Request location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        locationPermissions.launchMultiplePermissionRequest()
    }
    
    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> "home"
            is AuthState.Unauthenticated -> "login"
            else -> "login"
        }
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }
        
        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            val currentAuthState = authState
            if (currentAuthState is AuthState.Authenticated) {
                SimpleHomeScreen(
                    user = currentAuthState.user,
                    onNavigateToProfile = {
                        Log.d("MainActivity", "Navigating to profile")
                        navController.navigate("profile")
                    },
                    onNavigateToReports = {
                        Log.d("MainActivity", "Navigating to reports")
                        navController.navigate("reports")
                    },
                    onNavigateToLeaves = {
                        Log.d("MainActivity", "Navigating to leaves")
                        navController.navigate("leaves")
                    },
                    onNavigateToAdmin = {
                        Log.d("MainActivity", "Navigating to admin panel")
                        navController.navigate("admin")
                    },
                    onNavigateToDashboard = {
                        Log.d("MainActivity", "Navigating to dashboard")
                        navController.navigate("dashboard")
                    }
                )
            }
        }
        
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    Log.d("MainActivity", "Back navigation called from ProfileScreen")
                    navController.navigate("home") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }
        
        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    Log.d("MainActivity", "Back navigation called from ReportsScreen")
                    navController.navigate("home") {
                        popUpTo("reports") { inclusive = true }
                    }
                }
            )
        }
        
        composable("leaves") {
            LeaveManagementScreen(
                onNavigateBack = {
                    Log.d("MainActivity", "Back navigation called from LeaveManagementScreen")
                    navController.navigate("home") {
                        popUpTo("leaves") { inclusive = true }
                    }
                }
            )
        }
        
        composable("admin") {
            // TODO: Create AdminScreen
            // For now, show a placeholder
            AdminPlaceholderScreen(
                onNavigateBack = {
                    Log.d("MainActivity", "Back navigation called from AdminScreen")
                    navController.navigate("home") {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            // TODO: Create DashboardScreen
            // For now, show a placeholder
            DashboardPlaceholderScreen(
                onNavigateBack = {
                    Log.d("MainActivity", "Back navigation called from DashboardScreen")
                    navController.navigate("home") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
    
    // Handle navigation based on auth state changes
    LaunchedEffect(authState) {
        Log.d("MainActivity", "Auth state changed: $authState")
        Log.d("MainActivity", "Current destination: ${navController.currentDestination?.route}")
        
        val currentAuthState = authState
        when (currentAuthState) {
            is AuthState.Authenticated -> {
                Log.d("MainActivity", "User authenticated: ${currentAuthState.user.firstName}")
                val currentRoute = navController.currentDestination?.route
                // Only navigate to home if we're on login/signup screens, not from other authenticated screens
                if (currentRoute == "login" || currentRoute == "signup") {
                    Log.d("MainActivity", "Navigating to home screen from $currentRoute")
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                Log.d("MainActivity", "User unauthenticated")
                if (navController.currentDestination?.route != "login" && 
                    navController.currentDestination?.route != "signup") {
                    Log.d("MainActivity", "Navigating to login screen")
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Loading -> {
                Log.d("MainActivity", "Auth state loading")
                // Don't navigate during loading
            }
            is AuthState.Error -> {
                Log.e("MainActivity", "Auth error: ${currentAuthState.message}")
                // Stay on current screen to show error message
                // If we're on signup/login, the error will be displayed there
                // If we're elsewhere, we might need to handle this differently
                
                // If the error happens during initial app load, go to login
                if (navController.currentDestination == null) {
                    navController.navigate("login")
                }
            }
            else -> {
                Log.d("MainActivity", "Other auth state: $currentAuthState")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlaceholderScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Admin Panel",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Admin Panel",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Coming Soon!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This section will include:\n• User Management\n• System Settings\n• Reports & Analytics\n• Security Controls",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPlaceholderScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Dashboard,
                    contentDescription = "Dashboard",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Coming Soon!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This section will include:\n• Attendance Overview\n• Team Statistics\n• Performance Metrics\n• Quick Actions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}