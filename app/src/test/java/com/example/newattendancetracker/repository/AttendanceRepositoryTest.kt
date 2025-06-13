package com.example.newattendancetracker.repository

import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.model.Location
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

class AttendanceRepositoryTest {
    
    private lateinit var attendanceDao: AttendanceDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var attendanceRepository: AttendanceRepository
    
    @Before
    fun setup() {
        attendanceDao = mockk()
        firestore = mockk()
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
    fun `checkIn creates new attendance record successfully`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns null
        coEvery { attendanceDao.insertAttendance(any()) } just Runs
        
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection(any()) } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkIn(userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
    }
    
    @Test
    fun `checkIn updates existing attendance record`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        val existingAttendance = Attendance(
            id = "att123",
            userId = userId,
            date = Date(),
            checkInTime = null,
            status = AttendanceStatus.ABSENT
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns existingAttendance
        coEvery { attendanceDao.insertAttendance(any()) } just Runs
        
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection(any()) } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkIn(userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
    }
    
    @Test
    fun `checkOut updates existing attendance record`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        val existingAttendance = Attendance(
            id = "att123",
            userId = userId,
            date = Date(),
            checkInTime = Date(),
            status = AttendanceStatus.PRESENT
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns existingAttendance
        coEvery { attendanceDao.insertAttendance(any()) } just Runs
        
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection(any()) } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkOut(userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
    }
    
    @Test
    fun `checkOut fails when user hasn't checked in`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns null
        
        // When
        val result = attendanceRepository.checkOut(userId, location)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("No check-in found for today", result.exceptionOrNull()?.message)
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