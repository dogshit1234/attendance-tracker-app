package com.example.newattendancetracker.data.service

import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.model.Location
import com.example.newattendancetracker.data.security.AttendanceSecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureAttendanceService @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val securityManager: AttendanceSecurityManager
) {
    
    /**
     * Record secure check-in
     */
    suspend fun checkIn(
        userId: String,
        location: Location,
        notes: String = ""
    ): Result<Attendance> {
        return try {
            val currentTime = Date()
            val attendance = Attendance(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = currentTime,
                checkInTime = currentTime,
                checkInLocation = location,
                status = AttendanceStatus.PRESENT,
                notes = notes,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Apply security measures
            val secureAttendance = applySecurityMeasures(attendance)
            
            attendanceDao.insertAttendance(secureAttendance)
            Result.success(secureAttendance)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Record secure check-out
     */
    suspend fun checkOut(
        attendanceId: String,
        location: Location,
        notes: String = ""
    ): Result<Attendance> {
        return try {
            val existingAttendance = attendanceDao.getAttendanceById(attendanceId)
                ?: return Result.failure(Exception("Attendance record not found"))
            
            // Verify integrity before updating
            if (!verifyAttendanceIntegrity(existingAttendance)) {
                attendanceDao.markAsTampered(attendanceId)
                return Result.failure(SecurityException("Attendance record has been tampered with"))
            }
            
            val currentTime = Date()
            val checkInTime = existingAttendance.checkInTime ?: return Result.failure(Exception("No check-in time found"))
            
            // Calculate total hours
            val totalHours = (currentTime.time - checkInTime.time) / (1000.0 * 60 * 60)
            val breakTime = existingAttendance.totalBreakTime
            val workingHours = totalHours - breakTime
            
            val updatedAttendance = existingAttendance.copy(
                checkOutTime = currentTime,
                checkOutLocation = location,
                totalHours = workingHours,
                notes = if (notes.isNotEmpty()) "${existingAttendance.notes}\n$notes" else existingAttendance.notes,
                updatedAt = currentTime
            )
            
            // Apply security measures to updated record
            val secureAttendance = applySecurityMeasures(updatedAttendance)
            
            attendanceDao.updateAttendance(secureAttendance)
            Result.success(secureAttendance)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get attendance records with integrity verification
     */
    fun getSecureAttendanceRecords(userId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByUser(userId).map { attendanceList ->
            attendanceList.map { attendance ->
                if (!verifyAttendanceIntegrity(attendance)) {
                    // Mark as tampered but still return the record
                    attendance.copy(tamperDetected = true)
                } else {
                    attendance
                }
            }
        }
    }
    
    /**
     * Get suspicious/tampered records
     */
    suspend fun getSuspiciousRecords(userId: String): List<Attendance> {
        val allRecords = attendanceDao.getAttendanceByUserIdSync(userId)
        return allRecords.filter { !verifyAttendanceIntegrity(it) }
    }
    
    /**
     * Get security report for admin
     */
    suspend fun getSecurityReport(): SecurityReport {
        val tamperedRecords = attendanceDao.getTamperedRecords()
        val currentFingerprint = securityManager.createDeviceFingerprint()
        val recordsFromOtherDevices = attendanceDao.getRecordsFromOtherDevices(currentFingerprint)
        
        return SecurityReport(
            totalTamperedRecords = tamperedRecords.size,
            recordsFromOtherDevices = recordsFromOtherDevices,
            lastSecurityCheck = Date(),
            deviceFingerprint = currentFingerprint
        )
    }
    
    /**
     * Apply security measures to attendance record
     */
    private fun applySecurityMeasures(attendance: Attendance): Attendance {
        val hash = securityManager.createAttendanceHash(attendance)
        val deviceFingerprint = securityManager.createDeviceFingerprint()
        val encryptedData = securityManager.encryptAttendanceData(
            "${attendance.checkInTime}_${attendance.checkOutTime}_${attendance.totalHours}"
        )
        
        return attendance.copy(
            securityHash = hash,
            deviceFingerprint = deviceFingerprint,
            encryptedData = encryptedData,
            tamperDetected = false
        )
    }
    
    /**
     * Verify attendance record integrity
     */
    private fun verifyAttendanceIntegrity(attendance: Attendance): Boolean {
        if (attendance.securityHash.isEmpty()) {
            // Old record without security, consider valid but apply security on next update
            return true
        }
        
        // Create a copy without security fields for hash verification
        val attendanceForHash = attendance.copy(
            securityHash = "",
            deviceFingerprint = "",
            encryptedData = "",
            tamperDetected = false
        )
        
        val currentHash = securityManager.createAttendanceHash(attendanceForHash)
        val hashValid = currentHash == attendance.securityHash
        
        val deviceValid = if (attendance.deviceFingerprint.isNotEmpty()) {
            securityManager.validateDeviceFingerprint(attendance.deviceFingerprint)
        } else {
            true // Old record without device fingerprint
        }
        
        return hashValid && deviceValid
    }
}

/**
 * Security report data class
 */
data class SecurityReport(
    val totalTamperedRecords: Int,
    val recordsFromOtherDevices: Int,
    val lastSecurityCheck: Date,
    val deviceFingerprint: String
)