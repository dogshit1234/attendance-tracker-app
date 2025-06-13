package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE employeeId = :employeeId")
    suspend fun getUserByEmployeeId(employeeId: String): User?
    
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY firstName ASC")
    fun getAllActiveUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByRole(role: UserRole): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE managerId = :managerId AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByManager(managerId: String): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE department = :department AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByDepartment(department: String): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE companyId = :companyId AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByCompany(companyId: String): Flow<List<User>>
    
    @Query("SELECT DISTINCT department FROM users WHERE isActive = 1 ORDER BY department ASC")
    suspend fun getAllDepartments(): List<String>
    
    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    suspend fun getActiveUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE role = :role AND isActive = 1")
    suspend fun getUserCountByRole(role: UserRole): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: String)
    
    @Query("UPDATE users SET isActive = 1 WHERE id = :userId")
    suspend fun activateUser(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}