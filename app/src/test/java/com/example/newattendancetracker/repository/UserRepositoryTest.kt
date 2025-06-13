package com.example.newattendancetracker.repository

import com.example.newattendancetracker.data.local.dao.UserDao
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import com.example.newattendancetracker.data.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

class UserRepositoryTest {
    
    private lateinit var userDao: UserDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        userDao = mockk()
        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        userRepository = UserRepository(userDao, firestore, auth)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getUserById returns user from local database`() = runTest {
        // Given
        val userId = "user123"
        val expectedUser = User(
            id = userId,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        coEvery { userDao.getUserById(userId) } returns expectedUser
        
        // When
        val result = userRepository.getUserById(userId)
        
        // Then
        assertEquals(expectedUser, result)
        coVerify { userDao.getUserById(userId) }
    }
    
    @Test
    fun `getUserByEmail returns user from local database`() = runTest {
        // Given
        val email = "test@example.com"
        val expectedUser = User(
            id = "user123",
            email = email,
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        coEvery { userDao.getUserByEmail(email) } returns expectedUser
        
        // When
        val result = userRepository.getUserByEmail(email)
        
        // Then
        assertEquals(expectedUser, result)
        coVerify { userDao.getUserByEmail(email) }
    }
    
    @Test
    fun `getAllActiveUsers returns flow of active users`() = runTest {
        // Given
        val activeUsers = listOf(
            User(id = "user1", email = "user1@example.com", firstName = "John", lastName = "Doe", role = UserRole.EMPLOYEE, isActive = true),
            User(id = "user2", email = "user2@example.com", firstName = "Jane", lastName = "Smith", role = UserRole.MANAGER, isActive = true)
        )
        
        every { userDao.getAllActiveUsers() } returns flowOf(activeUsers)
        
        // When
        val result = userRepository.getAllActiveUsers()
        
        // Then
        result.collect { users ->
            assertEquals(activeUsers, users)
            assertEquals(2, users.size)
            assertTrue(users.all { it.isActive })
        }
    }
    
    @Test
    fun `getUsersByRole returns users with specific role`() = runTest {
        // Given
        val role = UserRole.MANAGER
        val managers = listOf(
            User(id = "mgr1", email = "mgr1@example.com", firstName = "Alice", lastName = "Johnson", role = role),
            User(id = "mgr2", email = "mgr2@example.com", firstName = "Bob", lastName = "Wilson", role = role)
        )
        
        every { userDao.getUsersByRole(role) } returns flowOf(managers)
        
        // When
        val result = userRepository.getUsersByRole(role)
        
        // Then
        result.collect { users ->
            assertEquals(managers, users)
            assertTrue(users.all { it.role == role })
        }
    }
    
    @Test
    fun `insertUser saves user to local database`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        coEvery { userDao.insertUser(user) } just Runs
        
        // When
        userRepository.insertUser(user)
        
        // Then
        coVerify { userDao.insertUser(user) }
    }
    
    @Test
    fun `syncUserFromFirebase success`() = runTest {
        // Given
        val userId = "user123"
        val user = User(
            id = userId,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        val mockDocumentSnapshot = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot.exists() } returns true
        every { mockDocumentSnapshot.toObject(User::class.java) } returns user
        
        val mockTask = mockk<Task<DocumentSnapshot>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockDocumentSnapshot
        every { mockTask.exception } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.get() } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        coEvery { userDao.insertUser(user) } just Runs
        
        // Mock the await() extension function
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockDocumentSnapshot
        
        // When
        val result = userRepository.syncUserFromFirebase(userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify { userDao.insertUser(user) }
    }
    
    @Test
    fun `syncUserFromFirebase user not found`() = runTest {
        // Given
        val userId = "user123"
        
        val mockDocumentSnapshot = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot.exists() } returns false
        
        val mockTask = mockk<Task<DocumentSnapshot>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockDocumentSnapshot
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.get() } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockDocumentSnapshot
        
        // When
        val result = userRepository.syncUserFromFirebase(userId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `saveUserToFirebase success`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        every { mockTask.exception } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(user) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(user.id) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        coEvery { userDao.insertUser(user) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = userRepository.saveUserToFirebase(user)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.insertUser(user) }
        verify { mockDocumentRef.set(user) }
    }
    
    @Test
    fun `getCurrentUser returns current authenticated user`() = runTest {
        // Given
        val userId = "user123"
        val user = User(
            id = userId,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        val mockFirebaseUser = mockk<FirebaseUser>()
        every { mockFirebaseUser.uid } returns userId
        every { auth.currentUser } returns mockFirebaseUser
        
        coEvery { userDao.getUserById(userId) } returns user
        
        // When
        val result = userRepository.getCurrentUser()
        
        // Then
        assertEquals(user, result)
        coVerify { userDao.getUserById(userId) }
    }
    
    @Test
    fun `getCurrentUser returns null when no authenticated user`() = runTest {
        // Given
        every { auth.currentUser } returns null
        
        // When
        val result = userRepository.getCurrentUser()
        
        // Then
        assertNull(result)
        coVerify(exactly = 0) { userDao.getUserById(any()) }
    }
    
    @Test
    fun `updateUserProfile success`() = runTest {
        // Given
        val user = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.set(user) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(user.id) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        coEvery { userDao.updateUser(user) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = userRepository.updateUserProfile(user)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.updateUser(user) }
        verify { mockDocumentRef.set(user) }
    }
    
    @Test
    fun `deactivateUser success`() = runTest {
        // Given
        val userId = "user123"
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.update("isActive", false) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        coEvery { userDao.deactivateUser(userId) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = userRepository.deactivateUser(userId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.deactivateUser(userId) }
        verify { mockDocumentRef.update("isActive", false) }
    }
    
    @Test
    fun `activateUser success`() = runTest {
        // Given
        val userId = "user123"
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns null
        
        val mockDocumentRef = mockk<DocumentReference>()
        every { mockDocumentRef.update("isActive", true) } returns mockTask
        
        val mockCollectionRef = mockk<CollectionReference>()
        every { mockCollectionRef.document(userId) } returns mockDocumentRef
        
        every { firestore.collection("users") } returns mockCollectionRef
        coEvery { userDao.activateUser(userId) } just Runs
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        val result = userRepository.activateUser(userId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { userDao.activateUser(userId) }
        verify { mockDocumentRef.update("isActive", true) }
    }
}