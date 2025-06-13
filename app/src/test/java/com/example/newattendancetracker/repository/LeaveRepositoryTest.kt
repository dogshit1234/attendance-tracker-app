package com.example.newattendancetracker.repository

import com.example.newattendancetracker.data.local.dao.LeaveDao
import com.example.newattendancetracker.data.model.Leave
import com.example.newattendancetracker.data.model.LeaveStatus
import com.example.newattendancetracker.data.model.LeaveType
import com.example.newattendancetracker.data.repository.LeaveRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class LeaveRepositoryTest {
    
    private lateinit var leaveDao: LeaveDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var leaveRepository: LeaveRepository
    
    @Before
    fun setup() {
        leaveDao = mockk()
        firestore = mockk(relaxed = true)
        leaveRepository = LeaveRepository(leaveDao, firestore)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getLeaveById returns leave from local database`() = runTest {
        // Given
        val leaveId = "leave123"
        val expectedLeave = Leave(
            id = leaveId,
            userId = "user123",
            type = LeaveType.ANNUAL,
            startDate = Date(),
            endDate = Date(),
            reason = "Vacation",
            status = LeaveStatus.PENDING
        )
        
        coEvery { leaveDao.getLeaveById(leaveId) } returns expectedLeave
        
        // When
        val result = leaveRepository.getLeaveById(leaveId)
        
        // Then
        assertEquals(expectedLeave, result)
        coVerify { leaveDao.getLeaveById(leaveId) }
    }
    
    @Test
    fun `getLeavesByUser returns flow of user leaves`() = runTest {
        // Given
        val userId = "user123"
        val userLeaves = listOf(
            Leave(id = "leave1", userId = userId, type = LeaveType.ANNUAL, startDate = Date(), endDate = Date()),
            Leave(id = "leave2", userId = userId, type = LeaveType.SICK, startDate = Date(), endDate = Date())
        )
        
        every { leaveDao.getLeavesByUser(userId) } returns flowOf(userLeaves)
        
        // When
        val result = leaveRepository.getLeavesByUser(userId)
        
        // Then
        result.collect { leaves ->
            assertEquals(userLeaves, leaves)
            assertEquals(2, leaves.size)
            assertTrue(leaves.all { it.userId == userId })
        }
    }
    
    @Test
    fun `getLeavesByUserAndDateRange returns leaves in date range`() = runTest {
        // Given
        val userId = "user123"
        val startDate = Date()
        val endDate = Date()
        val leavesInRange = listOf(
            Leave(id = "leave1", userId = userId, type = LeaveType.ANNUAL, startDate = startDate, endDate = endDate)
        )
        
        every { leaveDao.getLeavesByUserAndDateRange(userId, startDate, endDate) } returns flowOf(leavesInRange)
        
        // When
        val result = leaveRepository.getLeavesByUserAndDateRange(userId, startDate, endDate)
        
        // Then
        result.collect { leaves ->
            assertEquals(leavesInRange, leaves)
        }
        verify { leaveDao.getLeavesByUserAndDateRange(userId, startDate, endDate) }
    }
    
    @Test
    fun `getLeavesByStatus returns leaves with specific status`() = runTest {
        // Given
        val status = LeaveStatus.APPROVED
        val approvedLeaves = listOf(
            Leave(id = "leave1", userId = "user1", type = LeaveType.ANNUAL, startDate = Date(), endDate = Date(), status = status),
            Leave(id = "leave2", userId = "user2", type = LeaveType.SICK, startDate = Date(), endDate = Date(), status = status)
        )
        
        every { leaveDao.getLeavesByStatus(status) } returns flowOf(approvedLeaves)
        
        // When
        val result = leaveRepository.getLeavesByStatus(status)
        
        // Then
        result.collect { leaves ->
            assertEquals(approvedLeaves, leaves)
            assertTrue(leaves.all { it.status == status })
        }
    }
    
    @Test
    fun `getPendingLeaves returns pending leaves`() = runTest {
        // Given
        val pendingLeaves = listOf(
            Leave(id = "leave1", userId = "user1", type = LeaveType.ANNUAL, startDate = Date(), endDate = Date(), status = LeaveStatus.PENDING),
            Leave(id = "leave2", userId = "user2", type = LeaveType.SICK, startDate = Date(), endDate = Date(), status = LeaveStatus.PENDING)
        )
        
        every { leaveDao.getPendingLeaves() } returns flowOf(pendingLeaves)
        
        // When
        val result = leaveRepository.getPendingLeaves()
        
        // Then
        result.collect { leaves ->
            assertEquals(pendingLeaves, leaves)
            assertTrue(leaves.all { it.status == LeaveStatus.PENDING })
        }
    }
    
    @Test
    fun `insertLeave saves leave to local database`() = runTest {
        // Given
        val leave = Leave(
            id = "leave123",
            userId = "user123",
            type = LeaveType.ANNUAL,
            startDate = Date(),
            endDate = Date(),
            reason = "Vacation"
        )
        
        coEvery { leaveDao.insertLeave(leave) } just Runs
        
        // When
        leaveRepository.insertLeave(leave)
        
        // Then
        coVerify { leaveDao.insertLeave(leave) }
    }
    
    @Test
    fun `submitLeaveRequest success`() = runTest {
        // Given
        val leave = Leave(
            id = "leave123",
            userId = "user123",
            type = LeaveType.ANNUAL,
            startDate = Date(),
            endDate = Date(),
            reason = "Vacation"
        )
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(leave) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(leave.id) } returns mockDocumentRef
        
        every { firestore.collection("leaves") } returns mockCollectionRef
        coEvery { leaveDao.insertLeave(leave) } just Runs
        
        // Mock the await() extension function
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = leaveRepository.submitLeaveRequest(leave)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { leaveDao.insertLeave(leave) }
        verify { mockDocumentRef.set(leave) }
    }
    
    @Test
    fun `submitLeaveRequest failure`() = runTest {
        // Given
        val leave = Leave(
            id = "leave123",
            userId = "user123",
            type = LeaveType.ANNUAL,
            startDate = Date(),
            endDate = Date(),
            reason = "Vacation"
        )
        val errorMessage = "Network error"
        
        val mockTask = mockk<Task<Void>>()
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(leave) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(leave.id) } returns mockDocumentRef
        
        every { firestore.collection("leaves") } returns mockCollectionRef
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } throws Exception(errorMessage)
        
        // When
        val result = leaveRepository.submitLeaveRequest(leave)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `updateLeaveStatus success`() = runTest {
        // Given
        val leaveId = "leave123"
        val newStatus = LeaveStatus.APPROVED
        val existingLeave = Leave(
            id = leaveId,
            userId = "user123",
            type = LeaveType.ANNUAL,
            startDate = Date(),
            endDate = Date(),
            reason = "Vacation",
            status = LeaveStatus.PENDING
        )
        
        coEvery { leaveDao.getLeaveById(leaveId) } returns existingLeave
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(leaveId) } returns mockDocumentRef
        
        every { firestore.collection("leaves") } returns mockCollectionRef
        coEvery { leaveDao.updateLeave(any()) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = leaveRepository.updateLeaveStatus(leaveId, newStatus)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { leaveDao.updateLeave(any()) }
        verify { mockDocumentRef.set(any()) }
    }
    
    @Test
    fun `updateLeaveStatus leave not found`() = runTest {
        // Given
        val leaveId = "leave123"
        val newStatus = LeaveStatus.APPROVED
        
        coEvery { leaveDao.getLeaveById(leaveId) } returns null
        
        // When
        val result = leaveRepository.updateLeaveStatus(leaveId, newStatus)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Leave not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `syncLeavesFromFirebase success`() = runTest {
        // Given
        val userId = "user123"
        val leaves = listOf(
            Leave(id = "leave1", userId = userId, type = LeaveType.ANNUAL, startDate = Date(), endDate = Date()),
            Leave(id = "leave2", userId = userId, type = LeaveType.SICK, startDate = Date(), endDate = Date())
        )
        
        val mockDocumentSnapshot1 = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot1.toObject(Leave::class.java) } returns leaves[0]
        
        val mockDocumentSnapshot2 = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot2.toObject(Leave::class.java) } returns leaves[1]
        
        val mockQuerySnapshot = mockk<QuerySnapshot>()
        every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot1, mockDocumentSnapshot2)
        
        val mockTask = mockk<Task<QuerySnapshot>>()
        every { mockTask.result } returns mockQuerySnapshot
        
        val mockQuery = mockk<Query>()
        every { mockQuery.get() } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.whereEqualTo("userId", userId) } returns mockQuery
        
        every { firestore.collection("leaves") } returns mockCollectionRef
        coEvery { leaveDao.insertLeaves(leaves) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockQuerySnapshot
        
        // When
        val result = leaveRepository.syncLeavesFromFirebase(userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(leaves, result.getOrNull())
        coVerify { leaveDao.insertLeaves(leaves) }
    }
    
    @Test
    fun `getApprovedLeavesByUserAndYear returns approved leaves for year`() = runTest {
        // Given
        val userId = "user123"
        val year = 2024
        val calendar = Calendar.getInstance()
        
        // January 1st of the year
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // December 31st of the year
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        val approvedLeaves = listOf(
            Leave(id = "leave1", userId = userId, type = LeaveType.ANNUAL, startDate = Date(), endDate = Date(), status = LeaveStatus.APPROVED)
        )
        
        coEvery { leaveDao.getApprovedLeavesByUserAndYear(userId, startDate, endDate) } returns approvedLeaves
        
        // When
        val result = leaveRepository.getApprovedLeavesByUserAndYear(userId, year)
        
        // Then
        assertEquals(approvedLeaves, result)
        coVerify { leaveDao.getApprovedLeavesByUserAndYear(userId, startDate, endDate) }
    }
    
    @Test
    fun `getTotalLeaveDaysByUserAndYear returns total leave days`() = runTest {
        // Given
        val userId = "user123"
        val year = 2024
        val totalDays = 15
        val calendar = Calendar.getInstance()
        
        // January 1st of the year
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        // December 31st of the year
        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time
        
        coEvery { leaveDao.getTotalLeaveDaysByUserAndYear(userId, startDate, endDate) } returns totalDays
        
        // When
        val result = leaveRepository.getTotalLeaveDaysByUserAndYear(userId, year)
        
        // Then
        assertEquals(totalDays, result)
        coVerify { leaveDao.getTotalLeaveDaysByUserAndYear(userId, startDate, endDate) }
    }
}