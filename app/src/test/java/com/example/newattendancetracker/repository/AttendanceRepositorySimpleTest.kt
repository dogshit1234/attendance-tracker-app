package com.example.newattendancetracker.repository

import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class AttendanceRepositorySimpleTest {
    
    private lateinit var attendanceDao: AttendanceDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var attendanceRepository: AttendanceRepository
    
    @Before
    fun setup() {
        attendanceDao = mockk()
        firestore = mockk(relaxed = true)
        attendanceRepository = AttendanceRepository(attendanceDao, firestore)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getAttendanceByUserAndDate returns attendance for current date`() = runTest {
        // Given
        val userId = "user123"
        val today = Date()
        val expectedAttendance = Attendance(
            id = "att123",
            userId = userId,
            date = today,
            checkInTime = today,
            status = AttendanceStatus.PRESENT
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns expectedAttendance
        
        // When
        val result = attendanceRepository.getAttendanceByUserAndDate(userId, today)
        
        // Then
        assertEquals(expectedAttendance, result)
        coVerify { attendanceDao.getAttendanceByUserAndDate(userId, any()) }
    }
    
    @Test
    fun `getAttendanceByUserAndDateRange returns flow of attendance records`() = runTest {
        // Given
        val userId = "user123"
        val startDate = Date()
        val endDate = Date()
        val expectedAttendance = listOf(
            Attendance(id = "att1", userId = userId, date = Date()),
            Attendance(id = "att2", userId = userId, date = Date())
        )
        
        every { attendanceDao.getAttendanceByUserAndDateRange(userId, any(), any()) } returns flowOf(expectedAttendance)
        
        // When
        val result = attendanceRepository.getAttendanceByUserAndDateRange(userId, startDate, endDate)
        
        // Then
        result.collect { attendanceList ->
            assertEquals(expectedAttendance, attendanceList)
        }
    }
    
    @Test
    fun `getTotalHoursByUserAndDateRange returns correct total`() = runTest {
        // Given
        val userId = "user123"
        val startDate = Date()
        val endDate = Date()
        val expectedTotal = 40.0
        
        coEvery { attendanceDao.getTotalHoursByUserAndDateRange(userId, any(), any()) } returns expectedTotal
        
        // When
        val result = attendanceRepository.getTotalHoursByUserAndDateRange(userId, startDate, endDate)
        
        // Then
        assertEquals(expectedTotal, result, 0.1)
        coVerify { attendanceDao.getTotalHoursByUserAndDateRange(userId, any(), any()) }
    }
    
    @Test
    fun `getPresentDaysByUserAndDateRange returns correct count`() = runTest {
        // Given
        val userId = "user123"
        val startDate = Date()
        val endDate = Date()
        val expectedCount = 20
        
        coEvery { attendanceDao.getPresentDaysByUserAndDateRange(userId, any(), any()) } returns expectedCount
        
        // When
        val result = attendanceRepository.getPresentDaysByUserAndDateRange(userId, startDate, endDate)
        
        // Then
        assertEquals(expectedCount, result)
        coVerify { attendanceDao.getPresentDaysByUserAndDateRange(userId, any(), any()) }
    }
}