package com.example.newattendancetracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import com.example.newattendancetracker.data.repository.UserRepository
import com.example.newattendancetracker.util.ErrorHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                loadUserData(firebaseUser.uid)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    private suspend fun loadUserData(userId: String) {
        try {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } else {
                // User not found in database, sync from Firebase
                val result = userRepository.syncUserFromFirebase(userId)
                if (result.isSuccess) {
                    val syncedUser = result.getOrNull()
                    if (syncedUser != null) {
                        _currentUser.value = syncedUser
                        _authState.value = AuthState.Authenticated(syncedUser)
                    } else {
                        _authState.value = AuthState.Error("Failed to sync user data")
                    }
                } else {
                    _authState.value = AuthState.Error("User data not found")
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    loadUserData(firebaseUser.uid)
                } else {
                    _authState.value = AuthState.Error("Sign in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        employeeId: String,
        department: String,
        position: String,
        role: UserRole = UserRole.EMPLOYEE
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign up process for email: $email")
                _authState.value = AuthState.Loading
                
                // Create Firebase user
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    Log.d(TAG, "Firebase user created successfully: ${firebaseUser.uid}")
                    
                    // Create user object with consistent phone field mapping
                    val user = User(
                        id = firebaseUser.uid,
                        email = email,
                        name = "$firstName $lastName", // Set combined name
                        firstName = firstName,
                        lastName = lastName,
                        phone = phoneNumber, // Use phone field
                        phoneNumber = phoneNumber, // Keep for backward compatibility
                        employeeId = employeeId,
                        department = department,
                        position = position,
                        role = role,
                        createdAt = Date(),
                        updatedAt = Date(),
                        joinDate = Date() // Set join date
                    )
                    
                    Log.d(TAG, "Saving user data to Firebase...")
                    Log.d(TAG, "User object: $user")
                    
                    val saveResult = userRepository.saveUserToFirebase(user)
                    if (saveResult.isSuccess) {
                        Log.d(TAG, "User data saved successfully, setting authenticated state")
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated(user)
                    } else {
                        val error = saveResult.exceptionOrNull()
                        Log.e(TAG, "Failed to save user data: ${error?.message}", error)
                        // Clean up Firebase user if database save fails
                        try {
                            firebaseUser.delete().await()
                            Log.d(TAG, "Cleaned up Firebase user after database save failure")
                        } catch (deleteException: Exception) {
                            Log.e(TAG, "Failed to clean up Firebase user", deleteException)
                        }
                        _authState.value = AuthState.Error("Failed to save user data: ${error?.message}")
                    }
                } else {
                    Log.e(TAG, "Firebase user is null after creation")
                    _authState.value = AuthState.Error("Account creation failed. Please try again.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign up failed with exception: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("email address is already in use") == true -> 
                        "This email is already registered. Please use a different email or try signing in."
                    e.message?.contains("weak password") == true -> 
                        "Password is too weak. Please choose a stronger password."
                    e.message?.contains("invalid email") == true -> 
                        "Please enter a valid email address."
                    e.message?.contains("network") == true -> 
                        "Network error. Please check your internet connection and try again."
                    else -> "Sign up failed: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process")
                auth.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                Log.d(TAG, "Sign out completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
                _authState.value = AuthState.Error(e.message ?: "Sign out failed")
            }
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.PasswordResetSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Password reset failed")
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}