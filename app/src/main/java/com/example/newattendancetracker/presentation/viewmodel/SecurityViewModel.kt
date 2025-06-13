package com.example.newattendancetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.service.SecurityReport
import com.example.newattendancetracker.data.service.SecureAttendanceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val secureAttendanceService: SecureAttendanceService
) : ViewModel() {
    
    private val _securityReport = MutableStateFlow<SecurityReport?>(null)
    val securityReport: StateFlow<SecurityReport?> = _securityReport.asStateFlow()
    
    private val _suspiciousRecords = MutableStateFlow<List<Attendance>>(emptyList())
    val suspiciousRecords: StateFlow<List<Attendance>> = _suspiciousRecords.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadSecurityData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load security report
                val report = secureAttendanceService.getSecurityReport()
                _securityReport.value = report
                
                // Load suspicious records for all users (admin view)
                // In a real app, you'd get the current user and check if they're admin
                val suspicious = secureAttendanceService.getSuspiciousRecords("all") // This would need to be implemented
                _suspiciousRecords.value = suspicious
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load security data"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshSecurityData() {
        loadSecurityData()
    }
    
    fun clearError() {
        _error.value = null
    }
}