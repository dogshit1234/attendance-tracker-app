package com.example.newattendancetracker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.newattendancetracker.data.local.converter.Converters
import com.example.newattendancetracker.data.local.dao.*
import com.example.newattendancetracker.data.model.*

@Database(
    entities = [
        User::class,
        Attendance::class,
        Leave::class,
        Company::class,
        Payroll::class,
        Shift::class,
        UserShift::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AttendanceDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun companyDao(): CompanyDao
    abstract fun payrollDao(): PayrollDao
    abstract fun shiftDao(): ShiftDao
    
    companion object {
        @Volatile
        private var INSTANCE: AttendanceDatabase? = null
        
        fun getDatabase(context: Context): AttendanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceDatabase::class.java,
                    "attendance_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}