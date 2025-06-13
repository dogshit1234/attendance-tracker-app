package com.example.newattendancetracker.di

import android.content.Context
import androidx.room.Room
import com.example.newattendancetracker.data.local.AttendanceDatabase
import com.example.newattendancetracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAttendanceDatabase(@ApplicationContext context: Context): AttendanceDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AttendanceDatabase::class.java,
            "attendance_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: AttendanceDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideAttendanceDao(database: AttendanceDatabase): AttendanceDao = database.attendanceDao()
    
    @Provides
    fun provideLeaveDao(database: AttendanceDatabase): LeaveDao = database.leaveDao()
    
    @Provides
    fun provideCompanyDao(database: AttendanceDatabase): CompanyDao = database.companyDao()
    
    @Provides
    fun providePayrollDao(database: AttendanceDatabase): PayrollDao = database.payrollDao()
}