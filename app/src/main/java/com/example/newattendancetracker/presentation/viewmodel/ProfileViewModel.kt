package com.example.newattendancetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    fun updateProfile(user: User) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                userRepository.updateUser(user)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update profile"
                )
            }
        }
    }
    
    fun changePassword() {
        // TODO: Implement password change functionality
        viewModelScope.launch {
            try {
                // Navigate to change password screen or show dialog
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to change password"
                )
            }
        }
    }
    
    fun openNotificationSettings() {
        // TODO: Navigate to notification settings
    }
    
    fun openPrivacySettings() {
        // TODO: Navigate to privacy settings
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "Starting logout process")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Sign out from Firebase
                auth.signOut()
                android.util.Log.d("ProfileViewModel", "Successfully signed out from Firebase")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true
                )
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Logout failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to logout"
                )
            }
        }
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val userId = getCurrentUserId()
                userRepository.getUserByIdFlow(userId).collect { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)