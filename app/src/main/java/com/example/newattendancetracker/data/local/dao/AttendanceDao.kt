package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AttendanceDao {
    
    @Query("SELECT * FROM attendance WHERE id = :attendanceId")
    suspend fun getAttendanceById(attendanceId: String): Attendance?
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND date = :date")
    suspend fun getAttendanceByUserAndDate(userId: String, date: Date): Attendance?
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND date = :date")
    fun getAttendanceByUserAndDateFlow(userId: String, date: Date): Flow<Attendance?>
    
    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY date DESC")
    fun getAttendanceByUser(userId: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getAttendanceByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY checkInTime ASC")
    fun getAttendanceByDate(date: Date): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, checkInTime ASC")
    fun getAttendanceByDateRange(startDate: Date, endDate: Date): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE status = :status ORDER BY date DESC")
    fun getAttendanceByStatus(status: AttendanceStatus): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND status = :status ORDER BY date DESC")
    fun getAttendanceByUserAndStatus(userId: String, status: AttendanceStatus): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE checkOutTime IS NULL AND checkInTime IS NOT NULL ORDER BY checkInTime DESC")
    fun getActiveAttendance(): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND checkOutTime IS NULL AND checkInTime IS NOT NULL")
    suspend fun getActiveAttendanceByUser(userId: String): Attendance?
    
    @Query("SELECT SUM(totalHours) FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND status = 'PRESENT'")
    suspend fun getTotalHoursByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Double?
    
    @Query("SELECT SUM(overtimeHours) FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getOvertimeHoursByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Double?
    
    @Query("SELECT COUNT(*) FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND status = 'PRESENT'")
    suspend fun getPresentDaysByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND isLate = 1")
    suspend fun getLateDaysByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND isEarlyCheckOut = 1")
    suspend fun getEarlyCheckoutDaysByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'PRESENT'")
    suspend fun getPresentCountByDate(date: Date): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'ABSENT'")
    suspend fun getAbsentCountByDate(date: Date): Int
    
    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND isLate = 1")
    suspend fun getLateCountByDate(date: Date): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)
    
    @Update
    suspend fun updateAttendance(attendance: Attendance)
    
    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
    
    @Query("DELETE FROM attendance WHERE id = :attendanceId")
    suspend fun deleteAttendanceById(attendanceId: String)
    
    @Query("DELETE FROM attendance WHERE userId = :userId")
    suspend fun deleteAttendanceByUser(userId: String)
    
    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()
    
    // Security-related queries
    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAttendanceByUserIdSync(userId: String): List<Attendance>
    
    @Query("SELECT * FROM attendance WHERE tamperDetected = 1")
    suspend fun getTamperedRecords(): List<Attendance>
    
    @Query("SELECT * FROM attendance WHERE userId = :userId AND tamperDetected = 1")
    suspend fun getTamperedRecordsByUser(userId: String): List<Attendance>
    
    @Query("UPDATE attendance SET tamperDetected = 1 WHERE id = :attendanceId")
    suspend fun markAsTampered(attendanceId: String)
    
    @Query("SELECT COUNT(*) FROM attendance WHERE deviceFingerprint != :currentFingerprint AND deviceFingerprint != ''")
    suspend fun getRecordsFromOtherDevices(currentFingerprint: String): Int
}