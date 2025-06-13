package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val startTime: String = "", // HH:mm format
    val endTime: String = "",   // HH:mm format
    val breakDuration: Int = 60, // minutes
    val workingDays: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Monday, 7=Sunday
    val companyId: String = "",
    val isActive: Boolean = true,
    val overtimeThreshold: Double = 8.0, // hours
    val lateThreshold: Int = 15, // minutes
    val earlyCheckoutThreshold: Int = 30, // minutes
    val allowFlexibleTiming: Boolean = false,
    val flexibilityWindow: Int = 30, // minutes before/after shift time
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Entity(tableName = "user_shifts")
data class UserShift(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val shiftId: String = "",
    val effectiveDate: Date = Date(),
    val endDate: Date? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date()
)

enum class ShiftType {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    FLEXIBLE,
    ROTATING
}

data class ShiftSchedule(
    val shift: Shift,
    val assignedUsers: List<User> = emptyList(),
    val effectiveDate: Date,
    val endDate: Date? = null
)

data class OvertimeRule(
    val id: String = "",
    val shiftId: String = "",
    val dailyThreshold: Double = 8.0, // hours
    val weeklyThreshold: Double = 40.0, // hours
    val monthlyThreshold: Double = 160.0, // hours
    val overtimeRate: Double = 1.5, // multiplier for overtime pay
    val doubleTimeThreshold: Double = 12.0, // hours for double time
    val doubleTimeRate: Double = 2.0,
    val weekendRate: Double = 1.5,
    val holidayRate: Double = 2.0,
    val isActive: Boolean = true
)