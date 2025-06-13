package com.example.newattendancetracker.repository

import com.example.newattendancetracker.data.local.dao.AttendanceDao
import com.example.newattendancetracker.data.model.Attendance
import com.example.newattendancetracker.data.model.AttendanceStatus
import com.example.newattendancetracker.data.model.Location
import com.example.newattendancetracker.data.repository.AttendanceRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AttendanceRepositoryFirebaseTest {
    
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
    fun `checkIn with Firebase sync success`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns null
        coEvery { attendanceDao.insertAttendance(any()) } just Runs
        
        // Mock Firebase Task with proper async handling
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        every { mockTask.exception } returns null
        every { mockTask.isCanceled } returns false
        every { mockTask.isComplete } returns true
        
        // Mock Firebase Task listeners
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnSuccessListener<Void>>()
            listener.onSuccess(null)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnCompleteListener<Void>>()
            listener.onComplete(mockTask)
            mockTask
        }
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection("attendance") } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkIn(userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
        verify { mockDocumentRef.set(any()) }
    }
    
    @Test
    fun `checkIn with Firebase sync failure`() = runTest {
        // Given
        val userId = "user123"
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Office"
        )
        
        coEvery { attendanceDao.getAttendanceByUserAndDate(userId, any()) } returns null
        coEvery { attendanceDao.insertAttendance(any()) } just Runs
        
        // Mock Firebase Task with failure
        val mockTask = mockk<Task<Void>>()
        val firebaseException = FirebaseFirestoreException("Network error", FirebaseFirestoreException.Code.UNAVAILABLE)
        
        every { mockTask.isSuccessful } returns false
        every { mockTask.result } throws firebaseException
        every { mockTask.exception } returns firebaseException
        every { mockTask.isCanceled } returns false
        every { mockTask.isComplete } returns true
        
        every { mockTask.addOnSuccessListener(any()) } returns mockTask
        every { mockTask.addOnFailureListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnFailureListener>()
            listener.onFailure(firebaseException)
            mockTask
        }
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnCompleteListener<Void>>()
            listener.onComplete(mockTask)
            mockTask
        }
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection("attendance") } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkIn(userId, location)
        
        // Then
        // Should still succeed locally even if Firebase sync fails
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
        verify { mockDocumentRef.set(any()) }
    }
    
    @Test
    fun `checkOut with Firebase sync success`() = runTest {
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
        
        // Mock Firebase Task with success
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        every { mockTask.exception } returns null
        every { mockTask.isCanceled } returns false
        every { mockTask.isComplete } returns true
        
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnSuccessListener<Void>>()
            listener.onSuccess(null)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnCompleteListener<Void>>()
            listener.onComplete(mockTask)
            mockTask
        }
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(any()) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection("attendance") } returns mockCollectionRef
        
        // When
        val result = attendanceRepository.checkOut(userId, location)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { attendanceDao.insertAttendance(any()) }
        verify { mockDocumentRef.set(any()) }
    }
    
    @Test
    fun `checkOut fails when no check-in found`() = runTest {
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
        coVerify(exactly = 0) { attendanceDao.insertAttendance(any()) }
    }
    
    @Test
    fun `Firebase batch operations with proper async handling`() = runTest {
        // Given
        val attendances = listOf(
            Attendance(id = "att1", userId = "user1", date = Date()),
            Attendance(id = "att2", userId = "user2", date = Date()),
            Attendance(id = "att3", userId = "user3", date = Date())
        )
        
        coEvery { attendanceDao.insertAttendances(any()) } just Runs
        
        // Mock Firebase WriteBatch
        val mockBatch = mockk<WriteBatch>()
        val mockTask = mockk<Task<Void>>()
        
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        every { mockTask.exception } returns null
        every { mockTask.isCanceled } returns false
        every { mockTask.isComplete } returns true
        
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnSuccessListener<Void>>()
            listener.onSuccess(null)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<com.google.android.gms.tasks.OnCompleteListener<Void>>()
            listener.onComplete(mockTask)
            mockTask
        }
        
        every { mockBatch.set(any<DocumentReference>(), any()) } returns mockBatch
        every { mockBatch.commit() } returns mockTask
        
        val mockDocumentRef = mockk<DocumentReference>()
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(any()) } returns mockDocumentRef
        
        every { firestore.collection("attendance") } returns mockCollectionRef
        every { firestore.batch() } returns mockBatch
        
        // When - This would be called by a batch sync method if it existed
        // For now, we'll test the individual operations
        attendances.forEach { attendance ->
            val result = attendanceRepository.checkIn(attendance.userId, 
                Location(40.7128, -74.0060, "Office"))
            assertTrue(result.isSuccess)
        }
        
        // Then
        coVerify(exactly = attendances.size) { attendanceDao.insertAttendance(any()) }
        verify(exactly = attendances.size) { mockDocumentRef.set(any()) }
    }
}