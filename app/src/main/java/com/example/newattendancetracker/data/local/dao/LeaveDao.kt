package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.Leave
import com.example.newattendancetracker.data.model.LeaveStatus
import com.example.newattendancetracker.data.model.LeaveType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LeaveDao {
    
    @Query("SELECT * FROM leaves WHERE id = :leaveId")
    suspend fun getLeaveById(leaveId: String): Leave?
    
    @Query("SELECT * FROM leaves WHERE userId = :userId ORDER BY appliedAt DESC")
    fun getLeavesByUser(userId: String): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE userId = :userId AND status = :status ORDER BY appliedAt DESC")
    fun getLeavesByUserAndStatus(userId: String, status: LeaveStatus): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE status = :status ORDER BY appliedAt DESC")
    fun getLeavesByStatus(status: LeaveStatus): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE leaveType = :leaveType ORDER BY appliedAt DESC")
    fun getLeavesByType(leaveType: LeaveType): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE startDate BETWEEN :startDate AND :endDate ORDER BY startDate ASC")
    fun getLeavesByDateRange(startDate: Date, endDate: Date): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE userId = :userId AND startDate BETWEEN :startDate AND :endDate ORDER BY startDate ASC")
    fun getLeavesByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE userId = :userId AND leaveType = :leaveType AND startDate BETWEEN :startDate AND :endDate")
    suspend fun getLeavesByUserTypeAndYear(userId: String, leaveType: LeaveType, startDate: Date, endDate: Date): List<Leave>
    
    @Query("SELECT SUM(totalDays) FROM leaves WHERE userId = :userId AND leaveType = :leaveType AND status = 'APPROVED' AND startDate BETWEEN :startDate AND :endDate")
    suspend fun getApprovedLeaveDaysByUserTypeAndYear(userId: String, leaveType: LeaveType, startDate: Date, endDate: Date): Int?
    
    @Query("SELECT SUM(totalDays) FROM leaves WHERE userId = :userId AND leaveType = :leaveType AND status = 'PENDING' AND startDate BETWEEN :startDate AND :endDate")
    suspend fun getPendingLeaveDaysByUserTypeAndYear(userId: String, leaveType: LeaveType, startDate: Date, endDate: Date): Int?
    
    @Query("SELECT COUNT(*) FROM leaves WHERE status = 'PENDING'")
    suspend fun getPendingLeaveCount(): Int
    
    @Query("SELECT COUNT(*) FROM leaves WHERE userId = :userId AND status = 'PENDING'")
    suspend fun getPendingLeaveCountByUser(userId: String): Int
    
    @Query("SELECT * FROM leaves WHERE (startDate <= :date AND endDate >= :date) AND status = 'APPROVED'")
    suspend fun getApprovedLeavesForDate(date: Date): List<Leave>
    
    @Query("SELECT * FROM leaves WHERE userId = :userId AND (startDate <= :date AND endDate >= :date) AND status = 'APPROVED'")
    suspend fun getApprovedLeaveForUserAndDate(userId: String, date: Date): Leave?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: Leave)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaves(leaves: List<Leave>)
    
    @Update
    suspend fun updateLeave(leave: Leave)
    
    @Delete
    suspend fun deleteLeave(leave: Leave)
    
    @Query("DELETE FROM leaves WHERE id = :leaveId")
    suspend fun deleteLeaveById(leaveId: String)
    
    @Query("DELETE FROM leaves WHERE userId = :userId")
    suspend fun deleteLeavesByUser(userId: String)
    
    @Query("DELETE FROM leaves")
    suspend fun deleteAllLeaves()
    
    // Additional methods needed by repository
    @Query("SELECT * FROM leaves WHERE status = 'PENDING' ORDER BY appliedAt DESC")
    fun getPendingLeaves(): Flow<List<Leave>>
    
    @Query("SELECT * FROM leaves WHERE userId = :userId AND status = 'APPROVED' AND startDate >= :startDate AND startDate <= :endDate")
    suspend fun getApprovedLeavesByUserAndYear(userId: String, startDate: Date, endDate: Date): List<Leave>
    
    @Query("SELECT SUM(totalDays) FROM leaves WHERE userId = :userId AND status = 'APPROVED' AND startDate >= :startDate AND startDate <= :endDate")
    suspend fun getTotalLeaveDaysByUserAndYear(userId: String, startDate: Date, endDate: Date): Int
}