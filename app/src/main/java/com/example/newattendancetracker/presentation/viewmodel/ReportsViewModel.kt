package com.example.newattendancetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.example.newattendancetracker.presentation.screen.ChartData
import com.example.newattendancetracker.presentation.screen.DetailedReport
import com.example.newattendancetracker.presentation.screen.ReportsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    init {
        initializeDateRange()
        loadReports()
    }
    
    private fun initializeDateRange() {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, -30) // Last 30 days
        val startDate = calendar.time
        
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
    }
    
    fun updateDateRange(startDate: Date, endDate: Date) {
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        loadReports()
    }
    
    fun refreshReports() {
        loadReports()
    }
    
    private fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentState = _uiState.value
                val userId = getCurrentUserId() // TODO: Get from auth
                
                // Load attendance data
                attendanceRepository.getAttendanceByUserAndDateRange(
                    userId, currentState.startDate, currentState.endDate
                ).collect { attendanceList ->
                    
                    // Calculate summary statistics
                    val totalHours = attendanceList.sumOf { it.totalHours }
                    val presentDays = attendanceList.count { it.checkInTime != null }
                    val absentDays = getDaysBetween(currentState.startDate, currentState.endDate) - presentDays
                    val overtimeHours = attendanceList.sumOf { it.overtimeHours }
                    
                    // Generate chart data
                    val chartData = generateChartData(attendanceList)
                    
                    // Generate detailed reports
                    val detailedReports = attendanceList.map { attendance ->
                        DetailedReport(
                            date = attendance.date,
                            status = attendance.status.name,
                            checkInTime = attendance.checkInTime?.let { dateFormat.format(it) },
                            checkOutTime = attendance.checkOutTime?.let { dateFormat.format(it) },
                            totalHours = attendance.totalHours
                        )
                    }.sortedByDescending { it.date }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalHours = totalHours,
                        presentDays = presentDays,
                        absentDays = absentDays,
                        overtimeHours = overtimeHours,
                        chartData = chartData,
                        detailedReports = detailedReports
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    private fun generateChartData(attendanceList: List<com.example.newattendancetracker.data.model.Attendance>): List<ChartData> {
        val calendar = Calendar.getInstance()
        val weekData = mutableMapOf<String, Float>()
        
        // Initialize week days
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        dayNames.forEach { weekData[it] = 0f }
        
        // Aggregate data by day of week
        attendanceList.forEach { attendance ->
            calendar.time = attendance.date
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> "Mon"
            }
            
            weekData[dayOfWeek] = (weekData[dayOfWeek] ?: 0f) + attendance.totalHours.toFloat()
        }
        
        return dayNames.map { day ->
            ChartData(label = day, value = weekData[day] ?: 0f)
        }
    }
    
    private fun getDaysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillis = endDate.time - startDate.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
    }
    
    private fun getCurrentUserId(): String {
        // TODO: Get from authentication service
        return "current_user_id"
    }
}