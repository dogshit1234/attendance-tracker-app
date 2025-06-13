package com.example.newattendancetracker.data.repository

import com.example.newattendancetracker.data.local.dao.UserDao
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    // Local Database Operations
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)
    
    fun getUserByIdFlow(userId: String): Flow<User?> = userDao.getUserByIdFlow(userId)
    
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    
    fun getAllActiveUsers(): Flow<List<User>> = userDao.getAllActiveUsers()
    
    fun getUsersByRole(role: UserRole): Flow<List<User>> = userDao.getUsersByRole(role)
    
    fun getUsersByManager(managerId: String): Flow<List<User>> = userDao.getUsersByManager(managerId)
    
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    
    // Firebase Operations
    suspend fun syncUserFromFirebase(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let {
                    userDao.insertUser(it)
                    Result.success(it)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveUserToFirebase(user: User): Result<Unit> {
        return try {
            android.util.Log.d("UserRepository", "Saving user to Firebase: ${user.id}")
            android.util.Log.d("UserRepository", "User data: email=${user.email}, name=${user.name}")
            
            // First check if the user already exists
            val existingDoc = firestore.collection("users").document(user.id).get().await()
            if (existingDoc.exists()) {
                android.util.Log.w("UserRepository", "User document already exists in Firestore")
                // If it exists but we're trying to create it, this might be a retry after a failure
                // Let's update it instead of failing
                firestore.collection("users").document(user.id).set(user).await()
                android.util.Log.d("UserRepository", "Updated existing user document in Firebase")
            } else {
                // Save to Firebase Firestore
                firestore.collection("users").document(user.id).set(user).await()
                android.util.Log.d("UserRepository", "Created new user document in Firebase successfully")
            }
            
            // Save to local database
            userDao.insertUser(user)
            android.util.Log.d("UserRepository", "User saved to local database successfully")
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to save user: ${e.message}", e)
            
            // Check for specific Firebase errors
            val errorMessage = when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    "Database permission denied. Please check Firestore security rules."
                e.message?.contains("UNAUTHENTICATED") == true -> 
                    "Authentication failed. Please try again."
                e.message?.contains("NETWORK") == true || e.message?.contains("network") == true -> 
                    "Network error. Please check your internet connection."
                e.message?.contains("UNAVAILABLE") == true -> 
                    "Firebase service temporarily unavailable. Please try again."
                e.message?.contains("ALREADY_EXISTS") == true ->
                    "User account already exists."
                else -> e.message ?: "Unknown error occurred while saving user data"
            }
            
            android.util.Log.e("UserRepository", "Detailed error: $errorMessage")
            Result.failure(Exception(errorMessage, e))
        }
    }
    
    suspend fun syncAllUsersFromFirebase(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            userDao.insertUsers(users)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): User? {
        val currentUserId = auth.currentUser?.uid
        return currentUserId?.let { getUserById(it) }
    }
    
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            // Update in Firebase
            firestore.collection("users").document(user.id).set(user).await()
            // Update in local database
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            // Update in Firebase
            firestore.collection("users").document(userId)
                .update("isActive", false).await()
            // Update in local database
            userDao.deactivateUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun activateUser(userId: String): Result<Unit> {
        return try {
            // Update in Firebase
            firestore.collection("users").document(userId)
                .update("isActive", true).await()
            // Update in local database
            userDao.activateUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}