package com.example.newattendancetracker.data.security

import android.content.Context
import android.util.Base64
import com.example.newattendancetracker.data.model.Attendance
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceSecurityManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/ECB/PKCS1Padding"
        private const val KEY_ALIAS = "attendance_key"
    }
    
    /**
     * Create a tamper-proof hash of attendance data
     * This makes it very difficult to modify attendance records
     */
    fun createAttendanceHash(attendance: Attendance): String {
        val data = "${attendance.userId}_${attendance.checkInTime}_${attendance.checkOutTime}_${attendance.checkInLocation?.address}"
        return createSHA256Hash(data)
    }
    
    /**
     * Verify if attendance data has been tampered with
     */
    fun verifyAttendanceIntegrity(attendance: Attendance, storedHash: String): Boolean {
        val currentHash = createAttendanceHash(attendance)
        return currentHash == storedHash
    }
    
    /**
     * Encrypt sensitive attendance data
     */
    fun encryptAttendanceData(data: String): String {
        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            data // Fallback to plain text if encryption fails
        }
    }
    
    /**
     * Decrypt attendance data
     */
    fun decryptAttendanceData(encryptedData: String): String {
        return try {
            val key = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            encryptedData // Fallback to encrypted text if decryption fails
        }
    }
    
    /**
     * Create device-specific fingerprint to detect if data was moved between devices
     */
    fun createDeviceFingerprint(): String {
        val serial = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                android.os.Build.getSerial()
            } catch (e: SecurityException) {
                "UNKNOWN" // Fallback if permission is not granted
            }
        } else {
            @Suppress("DEPRECATION")
            android.os.Build.SERIAL
        }
        val deviceInfo = "${android.os.Build.MODEL}_${serial}_${android.os.Build.FINGERPRINT}"
        return createSHA256Hash(deviceInfo)
    }
    
    /**
     * Validate that attendance was recorded on the same device
     */
    fun validateDeviceFingerprint(storedFingerprint: String): Boolean {
        val currentFingerprint = createDeviceFingerprint()
        return currentFingerprint == storedFingerprint
    }
    
    fun createSHA256Hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        val sharedPrefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val keyString = sharedPrefs.getString(KEY_ALIAS, null)
        
        return if (keyString != null) {
            val keyBytes = Base64.decode(keyString, Base64.DEFAULT)
            SecretKeySpec(keyBytes, ALGORITHM)
        } else {
            // Generate new key
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(256)
            val secretKey = keyGenerator.generateKey()
            
            // Store key
            val keyBytes = secretKey.encoded
            val keyString64 = Base64.encodeToString(keyBytes, Base64.DEFAULT)
            sharedPrefs.edit().putString(KEY_ALIAS, keyString64).apply()
            
            secretKey
        }
    }
}