package com.example.newattendancetracker.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newattendancetracker.data.model.UserRole
import com.example.newattendancetracker.presentation.viewmodel.AuthState
import com.example.newattendancetracker.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    
    // Debug auth state changes
    LaunchedEffect(authState) {
        android.util.Log.d("SignUpScreen", "Auth state changed to: $authState")
        
        // Show error message if there's an error
        if (authState is AuthState.Error) {
            android.util.Log.e("SignUpScreen", "Auth error: ${(authState as AuthState.Error).message}")
        }
    }
    
    // Error state for auth errors
    var authError by remember { mutableStateOf<String?>(null) }
    
    // Update authError when authState changes
    LaunchedEffect(authState) {
        authError = if (authState is AuthState.Error) {
            (authState as AuthState.Error).message
        } else {
            null
        }
    }
    
    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var employeeIdError by remember { mutableStateOf("") }
    
    // Navigation is handled by MainActivity based on authState changes
    
    // Validation functions
    fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> ""
        }
    }
    
    fun validatePassword(password: String): String {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> ""
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): String {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> ""
        }
    }
    
    fun validateRequired(value: String, fieldName: String): String {
        return if (value.isBlank()) "$fieldName is required" else ""
    }
    
    fun validateForm(): Boolean {
        emailError = validateEmail(email)
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        firstNameError = validateRequired(firstName, "First name")
        lastNameError = validateRequired(lastName, "Last name")
        employeeIdError = validateRequired(employeeId, "Employee ID")
        
        return emailError.isEmpty() && passwordError.isEmpty() && 
               confirmPasswordError.isEmpty() && firstNameError.isEmpty() && 
               lastNameError.isEmpty() && employeeIdError.isEmpty()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Text(
            text = "Create Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Fill in your details to get started",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Personal Information Section
        Text(
            text = "Personal Information",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { 
                firstName = it
                firstNameError = ""
            },
            label = { Text("First Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "First Name") },
            isError = firstNameError.isNotEmpty(),
            supportingText = if (firstNameError.isNotEmpty()) {
                { Text(firstNameError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { 
                lastName = it
                lastNameError = ""
            },
            label = { Text("Last Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Last Name") },
            isError = lastNameError.isNotEmpty(),
            supportingText = if (lastNameError.isNotEmpty()) {
                { Text(lastNameError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = ""
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError.isNotEmpty(),
            supportingText = if (emailError.isNotEmpty()) {
                { Text(emailError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Phone Number
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number (Optional)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Work Information Section
        Text(
            text = "Work Information",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        // Employee ID
        OutlinedTextField(
            value = employeeId,
            onValueChange = { 
                employeeId = it
                employeeIdError = ""
            },
            label = { Text("Employee ID") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Employee ID") },
            isError = employeeIdError.isNotEmpty(),
            supportingText = if (employeeIdError.isNotEmpty()) {
                { Text(employeeIdError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Department
        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department (Optional)") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Department") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Position
        OutlinedTextField(
            value = position,
            onValueChange = { position = it },
            label = { Text("Position (Optional)") },
            leadingIcon = { Icon(Icons.Default.Work, contentDescription = "Position") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Role Selection
        ExposedDropdownMenuBox(
            expanded = showRoleDropdown,
            onExpandedChange = { showRoleDropdown = !showRoleDropdown },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = selectedRole.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = { },
                readOnly = true,
                label = { Text("Role") },
                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = showRoleDropdown,
                onDismissRequest = { showRoleDropdown = false }
            ) {
                UserRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        onClick = {
                            selectedRole = role
                            showRoleDropdown = false
                        },
                        leadingIcon = {
                            Icon(
                                when (role) {
                                    UserRole.ADMIN -> Icons.Default.SupervisorAccount
                                    UserRole.MANAGER -> Icons.Default.ManageAccounts
                                    UserRole.EMPLOYEE -> Icons.Default.Person
                                },
                                contentDescription = role.name
                            )
                        }
                    )
                }
            }
        }
        
        // Security Section
        Text(
            text = "Security",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = ""
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError.isNotEmpty(),
            supportingText = if (passwordError.isNotEmpty()) {
                { Text(passwordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                confirmPasswordError = ""
            },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = if (confirmPasswordError.isNotEmpty()) {
                { Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true
        )
        
        // Sign Up Button
        Button(
            onClick = {
                android.util.Log.d("SignUpScreen", "Create Account button clicked")
                android.util.Log.d("SignUpScreen", "Current auth state: $authState")
                if (validateForm()) {
                    android.util.Log.d("SignUpScreen", "Form validation passed, calling signUp")
                    viewModel.signUp(
                        email = email,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        employeeId = employeeId,
                        department = department.ifBlank { "General" },
                        position = position.ifBlank { "Employee" },
                        role = selectedRole
                    )
                } else {
                    android.util.Log.d("SignUpScreen", "Form validation failed")
                    android.util.Log.d("SignUpScreen", "Errors - email: $emailError, password: $passwordError, confirmPassword: $confirmPasswordError")
                    android.util.Log.d("SignUpScreen", "Errors - firstName: $firstNameError, lastName: $lastNameError, employeeId: $employeeIdError")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = authState !is AuthState.Authenticated,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auth Error Message
        if (authError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Sign In Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Sign In")
            }
        }
        
        // Error Message
        val currentAuthState = authState
        if (currentAuthState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentAuthState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}