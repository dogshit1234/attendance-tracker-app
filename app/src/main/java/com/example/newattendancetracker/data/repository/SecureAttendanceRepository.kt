package com.example.newattendancetracker.data.repository

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
class SecureAttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val securityManager: AttendanceSecurityManager
) {
    
    /**
     * Record attendance with security measures
     */
    suspend fun recordAttendance(
        userId: String,
        checkInTime: Date,
        location: String,
        latitude: Double,
        longitude: Double
    ): Result<Attendance> {
        return try {
            val attendance = Attendance(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = checkInTime,
                checkInTime = checkInTime,
                checkOutTime = null,
                checkInLocation = Location(latitude, longitude, location, 0f),
                status = AttendanceStatus.PRESENT,
                isManualEntry = false,
                notes = "",
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // Add security measures
            val secureAttendance = attendance.copy(
                // Create integrity hash
                notes = "hash:${securityManager.createAttendanceHash(attendance)}" +
                       "|device:${securityManager.createDeviceFingerprint()}" +
                       "|timestamp:${System.currentTimeMillis()}"
            )
            
            attendanceDao.insertAttendance(secureAttendance)
            Result.success(secureAttendance)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get attendance records with integrity verification
     */
    fun getAttendanceRecords(userId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByUser(userId).map { attendanceList ->
            attendanceList.filter { attendance ->
                // Verify integrity of each record
                verifyAttendanceIntegrity(attendance)
            }
        }
    }
    
    /**
     * Update attendance with security checks
     */
    suspend fun updateAttendance(attendance: Attendance): Result<Unit> {
        return try {
            // Verify the record hasn't been tampered with
            if (!verifyAttendanceIntegrity(attendance)) {
                return Result.failure(SecurityException("Attendance record has been tampered with"))
            }
            
            // Create new hash for updated record
            val updatedAttendance = attendance.copy(
                updatedAt = Date(),
                notes = "hash:${securityManager.createAttendanceHash(attendance)}" +
                       "|device:${securityManager.createDeviceFingerprint()}" +
                       "|timestamp:${System.currentTimeMillis()}"
            )
            
            attendanceDao.updateAttendance(updatedAttendance)
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify attendance record integrity
     */
    private fun verifyAttendanceIntegrity(attendance: Attendance): Boolean {
        val notes = attendance.notes ?: return false
        
        return try {
            // Extract stored hash and device fingerprint
            val parts = notes.split("|")
            val storedHash = parts.find { it.startsWith("hash:") }?.substringAfter("hash:")
            val storedDevice = parts.find { it.startsWith("device:") }?.substringAfter("device:")
            
            if (storedHash == null || storedDevice == null) return false
            
            // Verify hash integrity
            val currentHash = securityManager.createAttendanceHash(attendance)
            val hashValid = currentHash == storedHash
            
            // Verify device fingerprint
            val deviceValid = securityManager.validateDeviceFingerprint(storedDevice)
            
            hashValid && deviceValid
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get suspicious attendance records (potentially tampered)
     */
    suspend fun getSuspiciousRecords(userId: String): List<Attendance> {
        val allRecords = attendanceDao.getAttendanceByUserIdSync(userId)
        return allRecords.filter { !verifyAttendanceIntegrity(it) }
    }
}