package com.example.newattendancetracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.newattendancetracker.MainActivity
import com.example.newattendancetracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "attendance_reminders"
        const val CHANNEL_NAME = "Attendance Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for attendance check-in/out reminders"
        
        const val CHECK_IN_NOTIFICATION_ID = 1001
        const val CHECK_OUT_NOTIFICATION_ID = 1002
        const val LATE_ARRIVAL_NOTIFICATION_ID = 1003
        const val OVERTIME_NOTIFICATION_ID = 1004
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showCheckInReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "check_in")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to Check In!")
            .setContentText("Don't forget to mark your attendance for today")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check_in,
                "Check In Now",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(CHECK_IN_NOTIFICATION_ID, notification)
        }
    }
    
    fun showCheckOutReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "check_out")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to Check Out!")
            .setContentText("Remember to check out before leaving")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check_out,
                "Check Out Now",
                pendingIntent
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(CHECK_OUT_NOTIFICATION_ID, notification)
        }
    }
    
    fun showLateArrivalAlert(minutesLate: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Late Arrival Alert")
            .setContentText("You are $minutesLate minutes late today")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(LATE_ARRIVAL_NOTIFICATION_ID, notification)
        }
    }
    
    fun showOvertimeAlert(overtimeHours: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Overtime Alert")
            .setContentText("You have worked ${String.format("%.1f", overtimeHours)} hours of overtime today")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(OVERTIME_NOTIFICATION_ID, notification)
        }
    }
    
    fun cancelAllNotifications() {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
}