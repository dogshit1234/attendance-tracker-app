package com.example.newattendancetracker.data.repository

import com.example.newattendancetracker.data.local.dao.ShiftDao
import com.example.newattendancetracker.data.model.Shift
import com.example.newattendancetracker.data.model.UserShift
import com.example.newattendancetracker.data.model.OvertimeRule
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val shiftDao: ShiftDao
) {
    
    fun getActiveShiftsByCompany(companyId: String): Flow<List<Shift>> {
        return shiftDao.getActiveShiftsByCompany(companyId)
    }
    
    fun getAllShiftsByCompany(companyId: String): Flow<List<Shift>> {
        return shiftDao.getAllShiftsByCompany(companyId)
    }
    
    suspend fun getCurrentShiftForUser(userId: String): Shift? {
        return shiftDao.getCurrentShiftForUser(userId)
    }
    
    suspend fun getUserShiftForDate(userId: String, date: Date): UserShift? {
        return shiftDao.getUserShiftForDate(userId, date)
    }
    
    fun getUserShifts(userId: String): Flow<List<UserShift>> {
        return shiftDao.getUserShifts(userId)
    }
    
    fun getUsersInShift(shiftId: String): Flow<List<UserShift>> {
        return shiftDao.getUsersInShift(shiftId)
    }
    
    suspend fun createShift(shift: Shift): Result<Unit> {
        return try {
            shiftDao.insertShift(shift)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateShift(shift: Shift): Result<Unit> {
        return try {
            shiftDao.updateShift(shift)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignUserToShift(
        userId: String,
        shiftId: String,
        effectiveDate: Date = Date(),
        endDate: Date? = null
    ): Result<Unit> {
        return try {
            // Deactivate current shifts for the user
            shiftDao.deactivateUserShifts(userId, effectiveDate)
            
            // Create new shift assignment
            val userShift = UserShift(
                id = UUID.randomUUID().toString(),
                userId = userId,
                shiftId = shiftId,
                effectiveDate = effectiveDate,
                endDate = endDate,
                isActive = true
            )
            
            shiftDao.insertUserShift(userShift)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeUserFromShift(userId: String, endDate: Date = Date()): Result<Unit> {
        return try {
            shiftDao.deactivateUserShifts(userId, endDate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteShift(shiftId: String): Result<Unit> {
        return try {
            // Check if there are active users in this shift
            val activeUsersCount = shiftDao.getActiveUsersCountInShift(shiftId)
            if (activeUsersCount > 0) {
                return Result.failure(Exception("Cannot delete shift with active users"))
            }
            
            shiftDao.deleteShiftById(shiftId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deactivateShift(shiftId: String): Result<Unit> {
        return try {
            shiftDao.deactivateShift(shiftId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun calculateOvertimeHours(
        userId: String,
        date: Date,
        workedHours: Double
    ): Double {
        val userShift = getUserShiftForDate(userId, date)
        val shift = userShift?.let { shiftDao.getShiftById(it.shiftId) }
        
        return if (shift != null && workedHours > shift.overtimeThreshold) {
            workedHours - shift.overtimeThreshold
        } else {
            0.0
        }
    }
    
    suspend fun isUserLate(
        userId: String,
        checkInTime: Date,
        scheduledDate: Date
    ): Boolean {
        val userShift = getUserShiftForDate(userId, scheduledDate)
        val shift = userShift?.let { shiftDao.getShiftById(it.shiftId) }
        
        if (shift == null) return false
        
        val calendar = Calendar.getInstance().apply {
            time = scheduledDate
            val timeParts = shift.startTime.split(":")
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val scheduledStartTime = calendar.time
        val lateThresholdMs = shift.lateThreshold * 60 * 1000L
        
        return checkInTime.time > (scheduledStartTime.time + lateThresholdMs)
    }
    
    suspend fun isEarlyCheckout(
        userId: String,
        checkOutTime: Date,
        scheduledDate: Date
    ): Boolean {
        val userShift = getUserShiftForDate(userId, scheduledDate)
        val shift = userShift?.let { shiftDao.getShiftById(it.shiftId) }
        
        if (shift == null) return false
        
        val calendar = Calendar.getInstance().apply {
            time = scheduledDate
            val timeParts = shift.endTime.split(":")
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val scheduledEndTime = calendar.time
        val earlyThresholdMs = shift.earlyCheckoutThreshold * 60 * 1000L
        
        return checkOutTime.time < (scheduledEndTime.time - earlyThresholdMs)
    }
}