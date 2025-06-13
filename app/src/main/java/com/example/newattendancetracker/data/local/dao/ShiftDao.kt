package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.Shift
import com.example.newattendancetracker.data.model.UserShift
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ShiftDao {
    
    @Query("SELECT * FROM shifts WHERE id = :shiftId")
    suspend fun getShiftById(shiftId: String): Shift?
    
    @Query("SELECT * FROM shifts WHERE companyId = :companyId AND isActive = 1")
    fun getActiveShiftsByCompany(companyId: String): Flow<List<Shift>>
    
    @Query("SELECT * FROM shifts WHERE companyId = :companyId")
    fun getAllShiftsByCompany(companyId: String): Flow<List<Shift>>
    
    @Query("SELECT s.* FROM shifts s INNER JOIN user_shifts us ON s.id = us.shiftId WHERE us.userId = :userId AND us.isActive = 1 AND (us.endDate IS NULL OR us.endDate > :currentDate)")
    suspend fun getCurrentShiftForUser(userId: String, currentDate: Date = Date()): Shift?
    
    @Query("SELECT * FROM user_shifts WHERE userId = :userId AND isActive = 1")
    fun getUserShifts(userId: String): Flow<List<UserShift>>
    
    @Query("SELECT * FROM user_shifts WHERE shiftId = :shiftId AND isActive = 1")
    fun getUsersInShift(shiftId: String): Flow<List<UserShift>>
    
    @Query("SELECT * FROM user_shifts WHERE userId = :userId AND effectiveDate <= :date AND (endDate IS NULL OR endDate >= :date)")
    suspend fun getUserShiftForDate(userId: String, date: Date): UserShift?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShifts(shifts: List<Shift>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserShift(userShift: UserShift)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserShifts(userShifts: List<UserShift>)
    
    @Update
    suspend fun updateShift(shift: Shift)
    
    @Update
    suspend fun updateUserShift(userShift: UserShift)
    
    @Delete
    suspend fun deleteShift(shift: Shift)
    
    @Delete
    suspend fun deleteUserShift(userShift: UserShift)
    
    @Query("DELETE FROM shifts WHERE id = :shiftId")
    suspend fun deleteShiftById(shiftId: String)
    
    @Query("DELETE FROM user_shifts WHERE id = :userShiftId")
    suspend fun deleteUserShiftById(userShiftId: String)
    
    @Query("UPDATE user_shifts SET isActive = 0, endDate = :endDate WHERE userId = :userId AND isActive = 1")
    suspend fun deactivateUserShifts(userId: String, endDate: Date = Date())
    
    @Query("UPDATE shifts SET isActive = 0 WHERE id = :shiftId")
    suspend fun deactivateShift(shiftId: String)
    
    @Query("SELECT COUNT(*) FROM user_shifts WHERE shiftId = :shiftId AND isActive = 1")
    suspend fun getActiveUsersCountInShift(shiftId: String): Int
    
    @Query("DELETE FROM shifts")
    suspend fun deleteAllShifts()
    
    @Query("DELETE FROM user_shifts")
    suspend fun deleteAllUserShifts()
}