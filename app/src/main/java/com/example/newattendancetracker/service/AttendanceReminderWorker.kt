package com.example.newattendancetracker.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.example.newattendancetracker.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class AttendanceReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationService: NotificationService,
    private val attendanceRepository: AttendanceRepository,
    private val userRepository: UserRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "attendance_reminder_work"
        const val REMINDER_TYPE_KEY = "reminder_type"
        
        const val CHECK_IN_REMINDER = "check_in"
        const val CHECK_OUT_REMINDER = "check_out"
        
        fun scheduleCheckInReminder(context: Context, hour: Int, minute: Int) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                
                // If the time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            
            val workRequest = OneTimeWorkRequestBuilder<AttendanceReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(REMINDER_TYPE_KEY, CHECK_IN_REMINDER)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_check_in",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
        
        fun scheduleCheckOutReminder(context: Context, hour: Int, minute: Int) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                
                // If the time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            
            val workRequest = OneTimeWorkRequestBuilder<AttendanceReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(REMINDER_TYPE_KEY, CHECK_OUT_REMINDER)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_check_out",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
        
        fun cancelAllReminders(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("${WORK_NAME}_check_in")
            WorkManager.getInstance(context)
                .cancelUniqueWork("${WORK_NAME}_check_out")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val reminderType = inputData.getString(REMINDER_TYPE_KEY) ?: return Result.failure()
            val currentUser = userRepository.getCurrentUser()
            
            if (currentUser == null) {
                return Result.failure()
            }
            
            val today = Date()
            
            when (reminderType) {
                CHECK_IN_REMINDER -> {
                    // Check if user hasn't checked in yet today
                    val todayAttendance = attendanceRepository.getAttendanceByUserAndDate(currentUser.id, today)
                    if (todayAttendance == null) {
                        notificationService.showCheckInReminder()
                    }
                }
                CHECK_OUT_REMINDER -> {
                    // Check if user has checked in but not checked out
                    val todayAttendance = attendanceRepository.getAttendanceByUserAndDate(currentUser.id, today)
                    if (todayAttendance != null && todayAttendance.checkOutTime == null) {
                        notificationService.showCheckOutReminder()
                    }
                }
            }
            
            // Schedule the next reminder for tomorrow
            scheduleNextReminder(reminderType)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun scheduleNextReminder(reminderType: String) {
        when (reminderType) {
            CHECK_IN_REMINDER -> {
                // Schedule for tomorrow at the same time
                scheduleCheckInReminder(applicationContext, 8, 30) // Default 8:30 AM
            }
            CHECK_OUT_REMINDER -> {
                // Schedule for tomorrow at the same time
                scheduleCheckOutReminder(applicationContext, 17, 30) // Default 5:30 PM
            }
        }
    }
}