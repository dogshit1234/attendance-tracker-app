package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val date: Date = Date(),
    val checkInTime: Date? = null,
    val checkOutTime: Date? = null,
    val checkInLocation: Location? = null,
    val checkOutLocation: Location? = null,
    val totalHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val status: AttendanceStatus = AttendanceStatus.ABSENT,
    val notes: String = "",
    val isManualEntry: Boolean = false,
    val approvedBy: String? = null,
    val approvedAt: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val breakStartTime: Date? = null,
    val breakEndTime: Date? = null,
    val totalBreakTime: Double = 0.0,
    val isLate: Boolean = false,
    val isEarlyCheckOut: Boolean = false,
    val workFromHome: Boolean = false,
    // Security fields
    val securityHash: String = "",
    val deviceFingerprint: String = "",
    val encryptedData: String = "",
    val tamperDetected: Boolean = false
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val accuracy: Float = 0f
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    HALF_DAY,
    ON_LEAVE,
    WORK_FROM_HOME,
    PENDING_APPROVAL
}

data class AttendanceWithUser(
    val attendance: Attendance,
    val user: User
)

data class DailyAttendanceSummary(
    val date: Date,
    val totalEmployees: Int,
    val presentEmployees: Int,
    val absentEmployees: Int,
    val lateEmployees: Int,
    val onLeaveEmployees: Int,
    val workFromHomeEmployees: Int
)