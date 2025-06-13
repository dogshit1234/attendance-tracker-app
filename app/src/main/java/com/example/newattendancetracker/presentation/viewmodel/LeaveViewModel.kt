package com.example.newattendancetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newattendancetracker.data.model.LeaveStatus
import com.example.newattendancetracker.data.model.LeaveType
import com.example.newattendancetracker.data.repository.LeaveRepository
import com.example.newattendancetracker.presentation.screen.LeaveFilter
import com.example.newattendancetracker.presentation.screen.LeaveRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LeaveUiState())
    val uiState: StateFlow<LeaveUiState> = _uiState.asStateFlow()
    
    init {
        loadLeaveData()
    }
    
    fun updateFilter(filter: LeaveFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        filterLeaves()
    }
    
    fun submitLeaveRequest(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            try {
                // Convert to domain model and save
                val leave = com.example.newattendancetracker.data.model.Leave(
                    id = leaveRequest.id,
                    userId = leaveRequest.userId,
                    type = leaveRequest.type,
                    startDate = leaveRequest.startDate,
                    endDate = leaveRequest.endDate,
                    reason = leaveRequest.reason,
                    status = leaveRequest.status,
                    appliedDate = leaveRequest.appliedDate,
                    duration = leaveRequest.duration
                )
                
                leaveRepository.submitLeaveRequest(leave)
                loadLeaveData() // Refresh data
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to submit leave request"
                )
            }
        }
    }
    
    fun approveLeave(leaveId: String) {
        viewModelScope.launch {
            try {
                leaveRepository.updateLeaveStatus(leaveId, LeaveStatus.APPROVED)
                loadLeaveData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to approve leave"
                )
            }
        }
    }
    
    fun rejectLeave(leaveId: String) {
        viewModelScope.launch {
            try {
                leaveRepository.updateLeaveStatus(leaveId, LeaveStatus.REJECTED)
                loadLeaveData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reject leave"
                )
            }
        }
    }
    
    fun cancelLeave(leaveId: String) {
        viewModelScope.launch {
            try {
                leaveRepository.updateLeaveStatus(leaveId, LeaveStatus.CANCELLED)
                loadLeaveData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to cancel leave"
                )
            }
        }
    }
    
    private fun loadLeaveData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = getCurrentUserId()
                
                // Load leave requests
                leaveRepository.getLeavesByUser(userId).collect { leaves ->
                    val leaveRequests = leaves.map { leave ->
                        LeaveRequest(
                            id = leave.id,
                            userId = leave.userId,
                            type = leave.type,
                            startDate = leave.startDate,
                            endDate = leave.endDate,
                            reason = leave.reason,
                            status = leave.status,
                            appliedDate = leave.appliedDate,
                            duration = leave.duration
                        )
                    }
                    
                    // Calculate leave balance
                    val leaveBalance = calculateLeaveBalance(leaves)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allLeaves = leaveRequests,
                        leaveBalance = leaveBalance,
                        canManageLeaves = checkManagementPermissions()
                    )
                    
                    filterLeaves()
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load leave data"
                )
            }
        }
    }
    
    private fun filterLeaves() {
        val currentState = _uiState.value
        val filteredLeaves = when (currentState.selectedFilter) {
            LeaveFilter.ALL -> currentState.allLeaves
            LeaveFilter.PENDING -> currentState.allLeaves.filter { it.status == LeaveStatus.PENDING }
            LeaveFilter.APPROVED -> currentState.allLeaves.filter { it.status == LeaveStatus.APPROVED }
            LeaveFilter.REJECTED -> currentState.allLeaves.filter { it.status == LeaveStatus.REJECTED }
        }
        
        _uiState.value = currentState.copy(filteredLeaves = filteredLeaves)
    }
    
    private fun calculateLeaveBalance(leaves: List<com.example.newattendancetracker.data.model.Leave>): Map<LeaveType, Int> {
        val totalAllowance = mapOf(
            LeaveType.ANNUAL to 21,
            LeaveType.SICK to 10,
            LeaveType.PERSONAL to 5,
            LeaveType.MATERNITY to 90,
            LeaveType.PATERNITY to 15,
            LeaveType.EMERGENCY to 3
        )
        
        val usedLeaves = leaves
            .filter { it.status == LeaveStatus.APPROVED }
            .groupBy { it.type }
            .mapValues { (_, leaveList) -> leaveList.sumOf { it.duration } }
        
        return totalAllowance.mapValues { (type, total) ->
            total - (usedLeaves[type] ?: 0)
        }
    }
    
    private fun checkManagementPermissions(): Boolean {
        // TODO: Check user role from authentication
        return false // For now, assume regular employee
    }
    
    private fun getCurrentUserId(): String {
        // TODO: Get from authentication service
        return "current_user_id"
    }
}

data class LeaveUiState(
    val isLoading: Boolean = false,
    val selectedFilter: LeaveFilter = LeaveFilter.ALL,
    val allLeaves: List<LeaveRequest> = emptyList(),
    val filteredLeaves: List<LeaveRequest> = emptyList(),
    val leaveBalance: Map<LeaveType, Int> = emptyMap(),
    val canManageLeaves: Boolean = false,
    val error: String? = null
)