package com.example.newattendancetracker.debug

import android.util.Log
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupDebugHelper @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "SignupDebugHelper"
    }
    
    suspend fun testSignupProcess(
        email: String = "test@example.com",
        password: String = "password123",
        firstName: String = "Test",
        lastName: String = "User"
    ): SignupTestResult {
        return try {
            Log.d(TAG, "=== Starting Signup Debug Test ===")
            Log.d(TAG, "Email: $email")
            
            // Step 1: Test Firebase Auth
            Log.d(TAG, "Step 1: Creating Firebase user...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser == null) {
                return SignupTestResult.AuthFailed("Firebase user is null")
            }
            
            Log.d(TAG, "✅ Firebase user created: ${firebaseUser.uid}")
            
            // Step 2: Test User object creation
            Log.d(TAG, "Step 2: Creating User object...")
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = "$firstName $lastName",
                firstName = firstName,
                lastName = lastName,
                phone = "1234567890",
                phoneNumber = "1234567890",
                employeeId = "EMP001",
                department = "IT",
                position = "Developer",
                role = UserRole.ADMIN,
                createdAt = Date(),
                updatedAt = Date(),
                joinDate = Date()
            )
            
            Log.d(TAG, "✅ User object created: ${user.id}")
            
            // Step 3: Test Firestore save
            Log.d(TAG, "Step 3: Saving to Firestore...")
            firestore.collection("users").document(user.id).set(user).await()
            Log.d(TAG, "✅ User saved to Firestore successfully")
            
            // Step 4: Test Firestore read
            Log.d(TAG, "Step 4: Reading from Firestore...")
            val doc = firestore.collection("users").document(user.id).get().await()
            if (doc.exists()) {
                val retrievedUser = doc.toObject(User::class.java)
                Log.d(TAG, "✅ User retrieved from Firestore: ${retrievedUser?.email}")
            } else {
                Log.e(TAG, "❌ User document not found in Firestore")
                return SignupTestResult.FirestoreFailed("Document not found after save")
            }
            
            // Step 5: Cleanup
            Log.d(TAG, "Step 5: Cleaning up test user...")
            firebaseUser.delete().await()
            firestore.collection("users").document(user.id).delete().await()
            Log.d(TAG, "✅ Test user cleaned up")
            
            Log.d(TAG, "=== Signup Debug Test PASSED ===")
            SignupTestResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Signup test failed: ${e.message}", e)
            SignupTestResult.Failed(e.message ?: "Unknown error", e)
        }
    }
    
    suspend fun checkFirebaseConnection(): Boolean {
        return try {
            Log.d(TAG, "Testing Firebase connection...")
            val testDoc = firestore.collection("test").document("connection")
            testDoc.set(mapOf("timestamp" to Date())).await()
            testDoc.delete().await()
            Log.d(TAG, "✅ Firebase connection successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase connection failed: ${e.message}", e)
            false
        }
    }
    
    fun getCurrentAuthState(): String {
        val user = auth.currentUser
        return if (user != null) {
            "Authenticated: ${user.email} (${user.uid})"
        } else {
            "Not authenticated"
        }
    }
}

sealed class SignupTestResult {
    object Success : SignupTestResult()
    data class AuthFailed(val reason: String) : SignupTestResult()
    data class FirestoreFailed(val reason: String) : SignupTestResult()
    data class Failed(val message: String, val exception: Throwable) : SignupTestResult()
}