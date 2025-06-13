package com.example.newattendancetracker.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.newattendancetracker.data.local.AttendanceDatabase
import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.model.Location
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.*

@RunWith(AndroidJUnit4::class)
class AttendanceDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AttendanceDatabase
    private lateinit var attendanceDao: AttendanceDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AttendanceDatabase::class.java
        ).allowMainThreadQueries().build()
        
        attendanceDao = database.attendanceDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndGetAttendance() = runTest {
        // Given
        val attendance = Attendance(
            id = "att123",
            userId = "user123",
            date = Date(),
            checkInTime = Date(),
            checkInLocation = Location(latitude = 40.7128, longitude = -74.0060, address = "Office"),
            status = AttendanceStatus.PRESENT
        )
        
        // When
        attendanceDao.insertAttendance(attendance)
        val retrieved = attendanceDao.getAttendanceById("att123")
        
        // Then
        assertNotNull(retrieved)
        assertEquals(attendance.id, retrieved?.id)
        assertEquals(attendance.userId, retrieved?.userId)
        assertEquals(attendance.status, retrieved?.status)
    }
    
    @Test
    fun getTodayAttendance() = runTest {
        // Given
        val today = Date()
        val attendance = Attendance(
            id = "att123",
            userId = "user123",
            date = today,
            checkInTime = today,
            status = AttendanceStatus.PRESENT
        )
        
        // When
        attendanceDao.insertAttendance(attendance)
        val retrieved = attendanceDao.getAttendanceByUserAndDate("user123", today)
        
        // Then
        assertNotNull(retrieved)
        assertEquals(attendance.id, retrieved?.id)
    }
    
    @Test
    fun getMonthlyAttendance() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(2024, 0, 15) // January 15, 2024
        val date1 = calendar.time
        
        calendar.set(2024, 0, 20) // January 20, 2024
        val date2 = calendar.time
        
        val attendance1 = Attendance(
            id = "att1",
            userId = "user123",
            date = date1,
            checkInTime = date1,
            status = AttendanceStatus.PRESENT
        )
        
        val attendance2 = Attendance(
            id = "att2",
            userId = "user123",
            date = date2,
            checkInTime = date2,
            status = AttendanceStatus.PRESENT
        )
        
        // When
        attendanceDao.insertAttendance(attendance1)
        attendanceDao.insertAttendance(attendance2)
        
        calendar.set(2024, 0, 1) // Start of January
        val startDate = calendar.time
        calendar.set(2024, 0, 31) // End of January
        val endDate = calendar.time
        
        val monthlyAttendance = attendanceDao.getAttendanceByUserAndDateRange("user123", startDate, endDate).first()
        
        // Then
        assertEquals(2, monthlyAttendance.size)
        assertTrue(monthlyAttendance.any { it.id == "att1" })
        assertTrue(monthlyAttendance.any { it.id == "att2" })
    }
    
    @Test
    fun updateAttendance() = runTest {
        // Given
        val attendance = Attendance(
            id = "att123",
            userId = "user123",
            date = Date(),
            checkInTime = Date(),
            status = AttendanceStatus.PRESENT
        )
        
        // When
        attendanceDao.insertAttendance(attendance)
        
        val updatedAttendance = attendance.copy(
            checkOutTime = Date(),
            checkOutLocation = Location(latitude = 40.7128, longitude = -74.0060, address = "Office Exit")
        )
        attendanceDao.updateAttendance(updatedAttendance)
        
        val retrieved = attendanceDao.getAttendanceById("att123")
        
        // Then
        assertNotNull(retrieved)
        assertNotNull(retrieved?.checkOutTime)
        assertEquals("Office Exit", retrieved?.checkOutLocation?.address)
    }
    
    @Test
    fun deleteAttendance() = runTest {
        // Given
        val attendance = Attendance(
            id = "att123",
            userId = "user123",
            date = Date(),
            checkInTime = Date(),
            status = AttendanceStatus.PRESENT
        )
        
        // When
        attendanceDao.insertAttendance(attendance)
        attendanceDao.deleteAttendanceById("att123")
        val retrieved = attendanceDao.getAttendanceById("att123")
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun getAttendanceByDateRange() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        
        // Create attendance records for different dates
        calendar.set(2024, 0, 10)
        val date1 = calendar.time
        calendar.set(2024, 0, 15)
        val date2 = calendar.time
        calendar.set(2024, 0, 25)
        val date3 = calendar.time
        
        val attendance1 = Attendance(id = "att1", userId = "user123", date = date1, checkInTime = date1, status = AttendanceStatus.PRESENT)
        val attendance2 = Attendance(id = "att2", userId = "user123", date = date2, checkInTime = date2, status = AttendanceStatus.PRESENT)
        val attendance3 = Attendance(id = "att3", userId = "user123", date = date3, checkInTime = date3, status = AttendanceStatus.PRESENT)
        
        attendanceDao.insertAttendance(attendance1)
        attendanceDao.insertAttendance(attendance2)
        attendanceDao.insertAttendance(attendance3)
        
        // When - Query for records between Jan 12-20
        calendar.set(2024, 0, 12)
        val startDate = calendar.time
        calendar.set(2024, 0, 20)
        val endDate = calendar.time
        
        val result = attendanceDao.getAttendanceByUserAndDateRange("user123", startDate, endDate).first()
        
        // Then - Should only return attendance2 (Jan 15)
        assertEquals(1, result.size)
        assertEquals("att2", result[0].id)
    }
}