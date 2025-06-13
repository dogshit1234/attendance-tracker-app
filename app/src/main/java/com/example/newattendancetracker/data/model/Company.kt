package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val website: String = "",
    val logo: String = "",
    val workingHours: WorkingHours = WorkingHours(),
    val geofences: List<Geofence> = emptyList(),
    val settings: CompanySettings = CompanySettings(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class WorkingHours(
    val startTime: String = "09:00", // HH:mm format
    val endTime: String = "17:00",   // HH:mm format
    val breakStartTime: String = "12:00",
    val breakEndTime: String = "13:00",
    val workingDays: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Monday, 7=Sunday
    val overtimeThreshold: Double = 8.0, // hours per day
    val lateThreshold: Int = 15, // minutes
    val earlyCheckoutThreshold: Int = 30 // minutes before end time
)

data class CompanySettings(
    val allowManualAttendance: Boolean = false,
    val requireManagerApproval: Boolean = true,
    val allowWorkFromHome: Boolean = false,
    val enableGeofencing: Boolean = true,
    val enableBreakTracking: Boolean = true,
    val autoCheckoutAfterHours: Int = 12, // hours
    val notificationSettings: NotificationSettings = NotificationSettings()
)

data class NotificationSettings(
    val checkInReminder: Boolean = true,
    val checkOutReminder: Boolean = true,
    val lateArrivalAlert: Boolean = true,
    val overtimeAlert: Boolean = true,
    val reminderTime: String = "08:30" // HH:mm format
)