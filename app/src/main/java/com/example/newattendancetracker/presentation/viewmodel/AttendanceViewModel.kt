package com.example.newattendancetracker.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    
    private val _attendanceState = MutableStateFlow<AttendanceState>(AttendanceState.Loading)
    val attendanceState: StateFlow<AttendanceState> = _attendanceState.asStateFlow()
    
    private val _todayAttendance = MutableStateFlow<Attendance?>(null)
    val todayAttendance: StateFlow<Attendance?> = _todayAttendance.asStateFlow()
    
    private val _monthlyAttendance = MutableStateFlow<List<Attendance>>(emptyList())
    val monthlyAttendance: StateFlow<List<Attendance>> = _monthlyAttendance.asStateFlow()
    
    private val _isOnBreak = MutableStateFlow(false)
    val isOnBreak: StateFlow<Boolean> = _isOnBreak.asStateFlow()
    
    fun loadTodayAttendance(userId: String) {
        viewModelScope.launch {
            try {
                val today = Date()
                attendanceRepository.getAttendanceByUserAndDateFlow(userId, today)
                    .collect { attendance ->
                        _todayAttendance.value = attendance
                        _isOnBreak.value = attendance?.breakStartTime != null && attendance.breakEndTime == null
                        
                        if (attendance != null) {
                            _attendanceState.value = AttendanceState.Success
                        } else {
                            _attendanceState.value = AttendanceState.NotCheckedIn
                        }
                    }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Failed to load attendance")
            }
        }
    }
    
    fun loadMonthlyAttendance(userId: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = calendar.time
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = calendar.time
                
                attendanceRepository.getAttendanceByUserAndDateRange(userId, startOfMonth, endOfMonth)
                    .collect { attendances ->
                        _monthlyAttendance.value = attendances
                    }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Failed to load monthly attendance")
            }
        }
    }
    
    fun checkIn(userId: String) {
        viewModelScope.launch {
            try {
                _attendanceState.value = AttendanceState.Loading
                
                val location = getCurrentLocation()
                val attendanceLocation = com.example.newattendancetracker.data.model.Location(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                
                val result = attendanceRepository.checkIn(userId, attendanceLocation)
                if (result.isSuccess) {
                    _attendanceState.value = AttendanceState.CheckedIn
                    loadTodayAttendance(userId)
                } else {
                    _attendanceState.value = AttendanceState.Error(
                        result.exceptionOrNull()?.message ?: "Check-in failed"
                    )
                }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Check-in failed")
            }
        }
    }
    
    fun checkOut(userId: String) {
        viewModelScope.launch {
            try {
                _attendanceState.value = AttendanceState.Loading
                
                val location = getCurrentLocation()
                val attendanceLocation = com.example.newattendancetracker.data.model.Location(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                
                val result = attendanceRepository.checkOut(userId, attendanceLocation)
                if (result.isSuccess) {
                    _attendanceState.value = AttendanceState.CheckedOut
                    loadTodayAttendance(userId)
                } else {
                    _attendanceState.value = AttendanceState.Error(
                        result.exceptionOrNull()?.message ?: "Check-out failed"
                    )
                }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Check-out failed")
            }
        }
    }
    
    fun startBreak(userId: String) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.startBreak(userId)
                if (result.isSuccess) {
                    _isOnBreak.value = true
                    loadTodayAttendance(userId)
                } else {
                    _attendanceState.value = AttendanceState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to start break"
                    )
                }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Failed to start break")
            }
        }
    }
    
    fun endBreak(userId: String) {
        viewModelScope.launch {
            try {
                val result = attendanceRepository.endBreak(userId)
                if (result.isSuccess) {
                    _isOnBreak.value = false
                    loadTodayAttendance(userId)
                } else {
                    _attendanceState.value = AttendanceState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to end break"
                    )
                }
            } catch (e: Exception) {
                _attendanceState.value = AttendanceState.Error(e.message ?: "Failed to end break")
            }
        }
    }
    
    private suspend fun getCurrentLocation(): Location {
        return try {
            fusedLocationClient.lastLocation.await()
                ?: throw Exception("Unable to get current location")
        } catch (e: SecurityException) {
            throw Exception("Location permission not granted")
        }
    }
    
    fun getMonthlyStats(userId: String): MonthlyStats {
        val attendances = _monthlyAttendance.value
        val totalDays = attendances.size
        val presentDays = attendances.count { it.checkInTime != null }
        val totalHours = attendances.sumOf { it.totalHours }
        val overtimeHours = attendances.sumOf { it.overtimeHours }
        val lateDays = attendances.count { it.isLate }
        
        return MonthlyStats(
            totalDays = totalDays,
            presentDays = presentDays,
            absentDays = totalDays - presentDays,
            totalHours = totalHours,
            overtimeHours = overtimeHours,
            lateDays = lateDays,
            averageHoursPerDay = if (presentDays > 0) totalHours / presentDays else 0.0
        )
    }
}

sealed class AttendanceState {
    object Loading : AttendanceState()
    object Success : AttendanceState()
    object NotCheckedIn : AttendanceState()
    object CheckedIn : AttendanceState()
    object CheckedOut : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

data class MonthlyStats(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val totalHours: Double,
    val overtimeHours: Double,
    val lateDays: Int,
    val averageHoursPerDay: Double
)