package com.example.newattendancetracker.presentation.viewmodel

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: AttendanceViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        attendanceRepository = mockk()
        fusedLocationClient = mockk()
        
        viewModel = AttendanceViewModel(attendanceRepository, fusedLocationClient)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `loadTodayAttendance success with existing attendance`() = runTest {
        // Given
        val userId = "user123"
        val attendance = Attendance(
            id = "att123",
            userId = userId,
            date = Date(),
            checkInTime = Date(),
            status = AttendanceStatus.PRESENT
        )
        
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(attendance)
        
        // When
        viewModel.loadTodayAttendance(userId)
        
        // Then
        assertEquals(attendance, viewModel.todayAttendance.value)
        assertEquals(AttendanceState.Success, viewModel.attendanceState.value)
        assertFalse(viewModel.isOnBreak.value)
    }
    
    @Test
    fun `loadTodayAttendance success with no attendance`() = runTest {
        // Given
        val userId = "user123"
        
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(null)
        
        // When
        viewModel.loadTodayAttendance(userId)
        
        // Then
        assertNull(viewModel.todayAttendance.value)
        assertEquals(AttendanceState.NotCheckedIn, viewModel.attendanceState.value)
        assertFalse(viewModel.isOnBreak.value)
    }
    
    @Test
    fun `loadTodayAttendance with user on break`() = runTest {
        // Given
        val userId = "user123"
        val attendance = Attendance(
            id = "att123",
            userId = userId,
            date = Date(),
            checkInTime = Date(),
            breakStartTime = Date(),
            breakEndTime = null, // Still on break
            status = AttendanceStatus.PRESENT
        )
        
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(attendance)
        
        // When
        viewModel.loadTodayAttendance(userId)
        
        // Then
        assertEquals(attendance, viewModel.todayAttendance.value)
        assertEquals(AttendanceState.Success, viewModel.attendanceState.value)
        assertTrue(viewModel.isOnBreak.value)
    }
    
    @Test
    fun `loadTodayAttendance error handling`() = runTest {
        // Given
        val userId = "user123"
        val errorMessage = "Database error"
        
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } throws Exception(errorMessage)
        
        // When
        viewModel.loadTodayAttendance(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals(errorMessage, (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `loadMonthlyAttendance success`() = runTest {
        // Given
        val userId = "user123"
        val attendances = listOf(
            Attendance(id = "att1", userId = userId, date = Date()),
            Attendance(id = "att2", userId = userId, date = Date())
        )
        
        every { attendanceRepository.getAttendanceByUserAndDateRange(userId, any(), any()) } returns flowOf(attendances)
        
        // When
        viewModel.loadMonthlyAttendance(userId)
        
        // Then
        assertEquals(attendances, viewModel.monthlyAttendance.value)
    }
    
    @Test
    fun `checkIn success`() = runTest {
        // Given
        val userId = "user123"
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns 40.7128
        every { mockLocation.longitude } returns -74.0060
        every { mockLocation.accuracy } returns 10.0f
        
        val mockTask = mockk<Task<Location>>()
        every { mockTask.result } returns mockLocation
        every { fusedLocationClient.lastLocation } returns mockTask
        
        // Mock the await() extension function
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockLocation
        
        val mockAttendance = Attendance(id = "att123", userId = userId, date = Date())
        coEvery { attendanceRepository.checkIn(userId, any()) } returns Result.success(mockAttendance)
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(mockAttendance)
        
        // When
        viewModel.checkIn(userId)
        
        // Then
        assertEquals(AttendanceState.Success, viewModel.attendanceState.value)
        assertEquals(mockAttendance, viewModel.todayAttendance.value)
        coVerify { attendanceRepository.checkIn(userId, any()) }
    }
    
    @Test
    fun `checkIn failure`() = runTest {
        // Given
        val userId = "user123"
        val errorMessage = "Check-in failed"
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns 40.7128
        every { mockLocation.longitude } returns -74.0060
        every { mockLocation.accuracy } returns 10.0f
        
        val mockTask = mockk<Task<Location>>()
        every { mockTask.result } returns mockLocation
        every { fusedLocationClient.lastLocation } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockLocation
        
        coEvery { attendanceRepository.checkIn(userId, any()) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.checkIn(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals(errorMessage, (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `checkIn location permission error`() = runTest {
        // Given
        val userId = "user123"
        
        val mockTask = mockk<Task<Location>>()
        every { fusedLocationClient.lastLocation } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } throws SecurityException("Location permission not granted")
        
        // When
        viewModel.checkIn(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals("Location permission not granted", (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `checkOut success`() = runTest {
        // Given
        val userId = "user123"
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns 40.7128
        every { mockLocation.longitude } returns -74.0060
        every { mockLocation.accuracy } returns 10.0f
        
        val mockTask = mockk<Task<Location>>()
        every { mockTask.result } returns mockLocation
        every { fusedLocationClient.lastLocation } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockLocation
        
        val mockAttendance = Attendance(id = "att123", userId = userId, date = Date())
        coEvery { attendanceRepository.checkOut(userId, any()) } returns Result.success(mockAttendance)
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(mockAttendance)
        
        // When
        viewModel.checkOut(userId)
        
        // Then
        assertEquals(AttendanceState.Success, viewModel.attendanceState.value)
        assertEquals(mockAttendance, viewModel.todayAttendance.value)
        coVerify { attendanceRepository.checkOut(userId, any()) }
    }
    
    @Test
    fun `checkOut failure`() = runTest {
        // Given
        val userId = "user123"
        val errorMessage = "Check-out failed"
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns 40.7128
        every { mockLocation.longitude } returns -74.0060
        every { mockLocation.accuracy } returns 10.0f
        
        val mockTask = mockk<Task<Location>>()
        every { mockTask.result } returns mockLocation
        every { fusedLocationClient.lastLocation } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockLocation
        
        coEvery { attendanceRepository.checkOut(userId, any()) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.checkOut(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals(errorMessage, (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `startBreak success`() = runTest {
        // Given
        val userId = "user123"
        
        val mockAttendance = Attendance(
            id = "att123", 
            userId = userId, 
            date = Date(),
            breakStartTime = Date(),
            breakEndTime = null
        )
        coEvery { attendanceRepository.startBreak(userId) } returns Result.success(mockAttendance)
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(mockAttendance)
        
        // When
        viewModel.startBreak(userId)
        
        // Then
        assertTrue(viewModel.isOnBreak.value)
        coVerify { attendanceRepository.startBreak(userId) }
    }
    
    @Test
    fun `startBreak failure`() = runTest {
        // Given
        val userId = "user123"
        val errorMessage = "Failed to start break"
        
        coEvery { attendanceRepository.startBreak(userId) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.startBreak(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals(errorMessage, (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `endBreak success`() = runTest {
        // Given
        val userId = "user123"
        
        val mockAttendance = Attendance(id = "att123", userId = userId, date = Date())
        coEvery { attendanceRepository.endBreak(userId) } returns Result.success(mockAttendance)
        every { attendanceRepository.getAttendanceByUserAndDateFlow(userId, any()) } returns flowOf(null)
        
        // When
        viewModel.endBreak(userId)
        
        // Then
        assertFalse(viewModel.isOnBreak.value)
        coVerify { attendanceRepository.endBreak(userId) }
    }
    
    @Test
    fun `endBreak failure`() = runTest {
        // Given
        val userId = "user123"
        val errorMessage = "Failed to end break"
        
        coEvery { attendanceRepository.endBreak(userId) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.endBreak(userId)
        
        // Then
        assertTrue(viewModel.attendanceState.value is AttendanceState.Error)
        assertEquals(errorMessage, (viewModel.attendanceState.value as AttendanceState.Error).message)
    }
    
    @Test
    fun `getMonthlyStats calculates correctly`() = runTest {
        // Given
        val userId = "user123"
        val attendances = listOf(
            Attendance(
                id = "att1", 
                userId = userId, 
                date = Date(),
                checkInTime = Date(),
                totalHours = 8.0,
                overtimeHours = 1.0,
                isLate = true
            ),
            Attendance(
                id = "att2", 
                userId = userId, 
                date = Date(),
                checkInTime = Date(),
                totalHours = 7.5,
                overtimeHours = 0.0,
                isLate = false
            ),
            Attendance(
                id = "att3", 
                userId = userId, 
                date = Date(),
                checkInTime = null, // Absent
                totalHours = 0.0,
                overtimeHours = 0.0,
                isLate = false
            )
        )
        
        every { attendanceRepository.getAttendanceByUserAndDateRange(userId, any(), any()) } returns flowOf(attendances)
        
        // When
        viewModel.loadMonthlyAttendance(userId)
        val stats = viewModel.getMonthlyStats(userId)
        
        // Then
        assertEquals(3, stats.totalDays)
        assertEquals(2, stats.presentDays)
        assertEquals(1, stats.absentDays)
        assertEquals(15.5, stats.totalHours, 0.1)
        assertEquals(1.0, stats.overtimeHours, 0.1)
        assertEquals(1, stats.lateDays)
        assertEquals(7.75, stats.averageHoursPerDay, 0.1)
    }
    
    @Test
    fun `getMonthlyStats with no attendance`() = runTest {
        // Given
        val userId = "user123"
        val attendances = emptyList<Attendance>()
        
        every { attendanceRepository.getAttendanceByUserAndDateRange(userId, any(), any()) } returns flowOf(attendances)
        
        // When
        viewModel.loadMonthlyAttendance(userId)
        val stats = viewModel.getMonthlyStats(userId)
        
        // Then
        assertEquals(0, stats.totalDays)
        assertEquals(0, stats.presentDays)
        assertEquals(0, stats.absentDays)
        assertEquals(0.0, stats.totalHours, 0.1)
        assertEquals(0.0, stats.overtimeHours, 0.1)
        assertEquals(0, stats.lateDays)
        assertEquals(0.0, stats.averageHoursPerDay, 0.1)
    }
}