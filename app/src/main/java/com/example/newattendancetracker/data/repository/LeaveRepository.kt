package com.example.newattendancetracker.data.repository

import com.example.newattendancetracker.data.local.dao.LeaveDao
import com.example.newattendancetracker.data.model.Leave
import com.example.newattendancetracker.data.model.LeaveStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val leaveDao: LeaveDao,
    private val firestore: FirebaseFirestore
) {
    
    // Local Database Operations
    suspend fun getLeaveById(leaveId: String): Leave? = 
        leaveDao.getLeaveById(leaveId)
    
    fun getLeavesByUser(userId: String): Flow<List<Leave>> = 
        leaveDao.getLeavesByUser(userId)
    
    fun getLeavesByUserAndDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<Leave>> = 
        leaveDao.getLeavesByUserAndDateRange(userId, startDate, endDate)
    
    fun getLeavesByStatus(status: LeaveStatus): Flow<List<Leave>> = 
        leaveDao.getLeavesByStatus(status)
    
    fun getPendingLeaves(): Flow<List<Leave>> = 
        leaveDao.getPendingLeaves()
    
    suspend fun insertLeave(leave: Leave) = leaveDao.insertLeave(leave)
    
    suspend fun updateLeave(leave: Leave) = leaveDao.updateLeave(leave)
    
    suspend fun deleteLeave(leave: Leave) = leaveDao.deleteLeave(leave)
    
    // Firebase Operations
    suspend fun submitLeaveRequest(leave: Leave): Result<Unit> {
        return try {
            firestore.collection("leaves").document(leave.id).set(leave).await()
            leaveDao.insertLeave(leave)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLeaveStatus(leaveId: String, status: LeaveStatus): Result<Unit> {
        return try {
            val leave = leaveDao.getLeaveById(leaveId)
                ?: return Result.failure(Exception("Leave not found"))
            
            val updatedLeave = leave.copy(
                status = status,
                updatedAt = Date()
            )
            
            firestore.collection("leaves").document(leaveId).set(updatedLeave).await()
            leaveDao.updateLeave(updatedLeave)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncLeavesFromFirebase(userId: String): Result<List<Leave>> {
        return try {
            val snapshot = firestore.collection("leaves")
                .whereEqualTo("userId", userId)
                .get().await()
            
            val leaves = snapshot.documents.mapNotNull { it.toObject(Leave::class.java) }
            leaveDao.insertLeaves(leaves)
            Result.success(leaves)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Statistics
    suspend fun getApprovedLeavesByUserAndYear(userId: String, year: Int): List<Leave> {
        val calendar = Calendar.getInstance()
        
        // January 1st of the year
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // December 31st of the year
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        return leaveDao.getApprovedLeavesByUserAndYear(userId, startDate, endDate)
    }
    
    suspend fun getTotalLeaveDaysByUserAndYear(userId: String, year: Int): Int {
        val calendar = Calendar.getInstance()
        
        // January 1st of the year
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // December 31st of the year
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        return leaveDao.getTotalLeaveDaysByUserAndYear(userId, startDate, endDate)
    }
}