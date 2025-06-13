package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val email: String = "",
    val name: String = "", // Combined first and last name
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "", // Changed from phoneNumber
    val phoneNumber: String = "", // Keep for backward compatibility
    val role: UserRole = UserRole.EMPLOYEE,
    val department: String = "",
    val position: String = "",
    val employeeId: String = "",
    val profileImageUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val managerId: String? = null,
    val companyId: String = "",
    val hourlyRate: Double = 0.0,
    val monthlyRate: Double = 0.0,
    val overtimeRate: Double = 0.0,
    val workingHoursPerDay: Int = 8,
    val workingDaysPerWeek: Int = 5,
    // Additional fields for profile screen
    val joinDate: Date? = null,
    val workStartTime: String = "09:00",
    val workEndTime: String = "17:00",
    val workLocation: String = ""
)

enum class UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}

data class UserWithStats(
    val user: User,
    val totalHoursThisMonth: Double = 0.0,
    val totalDaysThisMonth: Int = 0,
    val overtimeHoursThisMonth: Double = 0.0,
    val lateCheckInsThisMonth: Int = 0,
    val earlyCheckOutsThisMonth: Int = 0
)