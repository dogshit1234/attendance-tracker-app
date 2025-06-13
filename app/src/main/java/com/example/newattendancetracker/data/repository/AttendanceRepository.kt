package com.example.newattendancetracker.data.repository

import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val firestore: FirebaseFirestore
) {
    
    // Local Database Operations
    suspend fun getAttendanceById(attendanceId: String): Attendance? = 
        attendanceDao.getAttendanceById(attendanceId)
    
    suspend fun getAttendanceByUserAndDate(userId: String, date: Date): Attendance? = 
        attendanceDao.getAttendanceByUserAndDate(userId, date)
    
    fun getAttendanceByUserAndDateFlow(userId: String, date: Date): Flow<Attendance?> = 
        attendanceDao.getAttendanceByUserAndDateFlow(userId, date)
    
    fun getAttendanceByUser(userId: String): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceByUser(userId)
    
    fun getAttendanceByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceByUserAndDateRange(userId, startDate, endDate)
    
    fun getAttendanceByDate(date: Date): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceByDate(date)
    
    fun getAttendanceByDateRange(startDate: Date, endDate: Date): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceByDateRange(startDate, endDate)
    
    suspend fun getActiveAttendanceByUser(userId: String): Attendance? = 
        attendanceDao.getActiveAttendanceByUser(userId)
    
    suspend fun insertAttendance(attendance: Attendance) = attendanceDao.insertAttendance(attendance)
    
    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.updateAttendance(attendance)
    
    // Firebase Operations
    suspend fun saveAttendanceToFirebase(attendance: Attendance): Result<Unit> {
        return try {
            firestore.collection("attendance").document(attendance.id).set(attendance).await()
            attendanceDao.insertAttendance(attendance)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncAttendanceFromFirebase(userId: String, startDate: Date, endDate: Date): Result<List<Attendance>> {
        return try {
            val snapshot = firestore.collection("attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get().await()
            
            val attendances = snapshot.documents.mapNotNull { it.toObject(Attendance::class.java) }
            attendanceDao.insertAttendances(attendances)
            Result.success(attendances)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkIn(userId: String, location: com.example.newattendancetracker.data.model.Location): Result<Attendance> {
        return try {
            val today = Date()
            val existingAttendance = getAttendanceByUserAndDate(userId, today)
            
            val attendance = if (existingAttendance != null) {
                existingAttendance.copy(
                    checkInTime = today,
                    checkInLocation = location,
                    status = AttendanceStatus.PRESENT,
                    updatedAt = today
                )
            } else {
                Attendance(
                    id = "${userId}_${System.currentTimeMillis()}",
                    userId = userId,
                    date = today,
                    checkInTime = today,
                    checkInLocation = location,
                    status = AttendanceStatus.PRESENT,
                    createdAt = today,
                    updatedAt = today
                )
            }
            
            saveAttendanceToFirebase(attendance)
            Result.success(attendance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkOut(userId: String, location: com.example.newattendancetracker.data.model.Location): Result<Attendance> {
        return try {
            val today = Date()
            val existingAttendance = getAttendanceByUserAndDate(userId, today)
                ?: return Result.failure(Exception("No check-in found for today"))
            
            val checkInTime = existingAttendance.checkInTime
                ?: return Result.failure(Exception("Check-in time not found"))
            
            val totalHours = (today.time - checkInTime.time) / (1000.0 * 60 * 60)
            val breakTime = existingAttendance.totalBreakTime
            val workingHours = totalHours - breakTime
            
            val updatedAttendance = existingAttendance.copy(
                checkOutTime = today,
                checkOutLocation = location,
                totalHours = workingHours,
                overtimeHours = if (workingHours > 8) workingHours - 8 else 0.0,
                updatedAt = today
            )
            
            saveAttendanceToFirebase(updatedAttendance)
            Result.success(updatedAttendance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startBreak(userId: String): Result<Attendance> {
        return try {
            val today = Date()
            val existingAttendance = getAttendanceByUserAndDate(userId, today)
                ?: return Result.failure(Exception("No check-in found for today"))
            
            val updatedAttendance = existingAttendance.copy(
                breakStartTime = today,
                updatedAt = today
            )
            
            saveAttendanceToFirebase(updatedAttendance)
            Result.success(updatedAttendance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endBreak(userId: String): Result<Attendance> {
        return try {
            val today = Date()
            val existingAttendance = getAttendanceByUserAndDate(userId, today)
                ?: return Result.failure(Exception("No check-in found for today"))
            
            val breakStartTime = existingAttendance.breakStartTime
                ?: return Result.failure(Exception("Break not started"))
            
            val breakDuration = (today.time - breakStartTime.time) / (1000.0 * 60 * 60)
            val totalBreakTime = existingAttendance.totalBreakTime + breakDuration
            
            val updatedAttendance = existingAttendance.copy(
                breakEndTime = today,
                totalBreakTime = totalBreakTime,
                breakStartTime = null, // Reset for next break
                updatedAt = today
            )
            
            saveAttendanceToFirebase(updatedAttendance)
            Result.success(updatedAttendance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Statistics
    suspend fun getTotalHoursByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Double {
        return attendanceDao.getTotalHoursByUserAndDateRange(userId, startDate, endDate) ?: 0.0
    }
    
    suspend fun getOvertimeHoursByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Double {
        return attendanceDao.getOvertimeHoursByUserAndDateRange(userId, startDate, endDate) ?: 0.0
    }
    
    suspend fun getPresentDaysByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Int {
        return attendanceDao.getPresentDaysByUserAndDateRange(userId, startDate, endDate)
    }
}