package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "leaves")
data class Leave(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val type: LeaveType = LeaveType.SICK, // Changed from leaveType
    val leaveType: LeaveType = LeaveType.SICK, // Keep for backward compatibility
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val duration: Int = 1, // Changed from totalDays
    val totalDays: Int = 1, // Keep for backward compatibility
    val reason: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING,
    val appliedDate: Date = Date(), // Changed from appliedAt
    val appliedAt: Date = Date(), // Keep for backward compatibility
    val reviewedBy: String? = null,
    val reviewedAt: Date? = null,
    val reviewComments: String = "",
    val attachments: List<String> = emptyList(),
    val isHalfDay: Boolean = false,
    val halfDayPeriod: HalfDayPeriod? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class LeaveType {
    SICK,
    CASUAL,
    ANNUAL,
    PERSONAL, // Added missing PERSONAL
    MATERNITY,
    PATERNITY,
    EMERGENCY,
    UNPAID,
    COMPENSATORY
}

enum class LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

enum class HalfDayPeriod {
    MORNING,
    AFTERNOON
}

data class LeaveBalance(
    val userId: String = "",
    val year: Int = 2024,
    val sickLeave: LeaveAllocation = LeaveAllocation(),
    val casualLeave: LeaveAllocation = LeaveAllocation(),
    val annualLeave: LeaveAllocation = LeaveAllocation(),
    val maternityLeave: LeaveAllocation = LeaveAllocation(),
    val paternityLeave: LeaveAllocation = LeaveAllocation(),
    val compensatoryLeave: LeaveAllocation = LeaveAllocation()
)

data class LeaveAllocation(
    val allocated: Int = 0,
    val used: Int = 0,
    val pending: Int = 0,
    val remaining: Int = 0
)

data class LeaveWithUser(
    val leave: Leave,
    val user: User
)