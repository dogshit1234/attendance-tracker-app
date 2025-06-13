package com.example.newattendancetracker.util

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor() {
    
    companion object {
        private const val TAG = "ErrorHandler"
    }
    
    fun handleError(
        throwable: Throwable,
        context: Context? = null,
        snackbarHostState: SnackbarHostState? = null,
        coroutineScope: CoroutineScope? = null,
        customMessage: String? = null
    ): String {
        val errorMessage = customMessage ?: getErrorMessage(throwable)
        
        // Log the error
        Log.e(TAG, "Error occurred: ${throwable.message}", throwable)
        
        // Show snackbar if provided
        snackbarHostState?.let { snackbar ->
            coroutineScope?.launch {
                snackbar.showSnackbar(errorMessage)
            }
        }
        
        return errorMessage
    }
    
    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthException -> handleFirebaseAuthError(throwable)
            is FirebaseFirestoreException -> handleFirestoreError(throwable)
            is FirebaseException -> "Firebase service error. Please try again."
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Please check your connection."
            is SecurityException -> "Permission denied. Please check app permissions."
            is IllegalArgumentException -> "Invalid input. Please check your data."
            is IllegalStateException -> "App is in an invalid state. Please restart the app."
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }
    
    private fun handleFirebaseAuthError(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address format."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
            "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please choose a stronger password."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
            else -> "Authentication failed. Please try again."
        }
    }
    
    private fun handleFirestoreError(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Access denied. Please check your permissions."
            FirebaseFirestoreException.Code.NOT_FOUND -> "Requested data not found."
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "Data already exists."
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Service quota exceeded. Please try again later."
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> "Operation failed due to precondition."
            FirebaseFirestoreException.Code.ABORTED -> "Operation was aborted. Please try again."
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> "Invalid data range."
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> "Feature not implemented."
            FirebaseFirestoreException.Code.INTERNAL -> "Internal server error. Please try again."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Service temporarily unavailable. Please try again."
            FirebaseFirestoreException.Code.DATA_LOSS -> "Data loss detected. Please contact support."
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Authentication required. Please sign in."
            FirebaseFirestoreException.Code.INVALID_ARGUMENT -> "Invalid request parameters."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Request timed out. Please try again."
            FirebaseFirestoreException.Code.CANCELLED -> "Operation was cancelled."
            else -> "Database error. Please try again."
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }
}