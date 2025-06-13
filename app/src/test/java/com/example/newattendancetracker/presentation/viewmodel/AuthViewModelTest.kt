package com.example.newattendancetracker.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.newattendancetracker.data.model.User
import com.example.newattendancetracker.data.model.UserRole
import com.example.newattendancetracker.data.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        auth = mockk()
        userRepository = mockk()
        
        // Mock initial auth state check
        every { auth.currentUser } returns null
        
        viewModel = AuthViewModel(auth, userRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `initial state is unauthenticated when no current user`() = runTest {
        // Given - setup already mocks auth.currentUser as null
        
        // Then
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
        assertNull(viewModel.currentUser.value)
    }
    
    @Test
    fun `initial state loads user data when current user exists`() = runTest {
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
        
        coEvery { userRepository.getUserById(userId) } returns user
        
        // When
        val newViewModel = AuthViewModel(auth, userRepository)
        
        // Then
        assertEquals(user, newViewModel.currentUser.value)
        assertTrue(newViewModel.authState.value is AuthState.Authenticated)
        assertEquals(user, (newViewModel.authState.value as AuthState.Authenticated).user)
    }
    
    @Test
    fun `initial state syncs user from Firebase when not found locally`() = runTest {
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
        
        coEvery { userRepository.getUserById(userId) } returns null
        coEvery { userRepository.syncUserFromFirebase(userId) } returns Result.success(user)
        
        // When
        val newViewModel = AuthViewModel(auth, userRepository)
        
        // Then
        assertEquals(user, newViewModel.currentUser.value)
        assertTrue(newViewModel.authState.value is AuthState.Authenticated)
        coVerify { userRepository.syncUserFromFirebase(userId) }
    }
    
    @Test
    fun `signIn success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"
        val user = User(
            id = userId,
            email = email,
            firstName = "John",
            lastName = "Doe",
            role = UserRole.EMPLOYEE
        )
        
        val mockFirebaseUser = mockk<FirebaseUser>()
        every { mockFirebaseUser.uid } returns userId
        
        val mockAuthResult = mockk<AuthResult>()
        every { mockAuthResult.user } returns mockFirebaseUser
        
        val mockTask = mockk<Task<AuthResult>>()
        every { mockTask.result } returns mockAuthResult
        every { auth.signInWithEmailAndPassword(email, password) } returns mockTask
        
        coEvery { userRepository.getUserById(userId) } returns user
        
        // Mock the await() extension function
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockAuthResult
        
        // When
        viewModel.signIn(email, password)
        
        // Then
        assertEquals(user, viewModel.currentUser.value)
        assertTrue(viewModel.authState.value is AuthState.Authenticated)
        verify { auth.signInWithEmailAndPassword(email, password) }
    }
    
    @Test
    fun `signIn failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        
        val mockTask = mockk<Task<AuthResult>>()
        every { auth.signInWithEmailAndPassword(email, password) } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } throws Exception(errorMessage)
        
        // When
        viewModel.signIn(email, password)
        
        // Then
        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
        assertNull(viewModel.currentUser.value)
    }
    
    @Test
    fun `signUp success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val firstName = "John"
        val lastName = "Doe"
        val phoneNumber = "1234567890"
        val employeeId = "EMP001"
        val department = "IT"
        val position = "Developer"
        val userId = "user123"
        
        val mockFirebaseUser = mockk<FirebaseUser>()
        every { mockFirebaseUser.uid } returns userId
        
        val mockAuthResult = mockk<AuthResult>()
        every { mockAuthResult.user } returns mockFirebaseUser
        
        val mockTask = mockk<Task<AuthResult>>()
        every { mockTask.result } returns mockAuthResult
        every { auth.createUserWithEmailAndPassword(email, password) } returns mockTask
        
        coEvery { userRepository.saveUserToFirebase(any()) } returns Result.success(Unit)
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockAuthResult
        
        // When
        viewModel.signUp(email, password, firstName, lastName, phoneNumber, employeeId, department, position)
        
        // Then
        assertNotNull(viewModel.currentUser.value)
        assertEquals(email, viewModel.currentUser.value?.email)
        assertEquals(firstName, viewModel.currentUser.value?.firstName)
        assertEquals(lastName, viewModel.currentUser.value?.lastName)
        assertTrue(viewModel.authState.value is AuthState.Authenticated)
        verify { auth.createUserWithEmailAndPassword(email, password) }
        coVerify { userRepository.saveUserToFirebase(any()) }
    }
    
    @Test
    fun `signUp failure during user creation`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Email already in use"
        
        val mockTask = mockk<Task<AuthResult>>()
        every { auth.createUserWithEmailAndPassword(email, password) } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } throws Exception(errorMessage)
        
        // When
        viewModel.signUp(email, password, "John", "Doe", "1234567890", "EMP001", "IT", "Developer")
        
        // Then
        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
        assertNull(viewModel.currentUser.value)
    }
    
    @Test
    fun `signUp failure during user data save`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"
        val errorMessage = "Failed to save user data"
        
        val mockFirebaseUser = mockk<FirebaseUser>()
        every { mockFirebaseUser.uid } returns userId
        
        val mockAuthResult = mockk<AuthResult>()
        every { mockAuthResult.user } returns mockFirebaseUser
        
        val mockTask = mockk<Task<AuthResult>>()
        every { mockTask.result } returns mockAuthResult
        every { auth.createUserWithEmailAndPassword(email, password) } returns mockTask
        
        coEvery { userRepository.saveUserToFirebase(any()) } returns Result.failure(Exception(errorMessage))
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockAuthResult
        
        // When
        viewModel.signUp(email, password, "John", "Doe", "1234567890", "EMP001", "IT", "Developer")
        
        // Then
        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
    }
    
    @Test
    fun `signOut success`() = runTest {
        // Given
        every { auth.signOut() } just Runs
        
        // When
        viewModel.signOut()
        
        // Then
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
        assertNull(viewModel.currentUser.value)
        verify { auth.signOut() }
    }
    
    @Test
    fun `resetPassword success`() = runTest {
        // Given
        val email = "test@example.com"
        
        val mockTask = mockk<Task<Void>>()
        every { mockTask.result } returns null
        every { auth.sendPasswordResetEmail(email) } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } returns mockk<Void>()
        
        // When
        viewModel.resetPassword(email)
        
        // Then
        assertEquals(AuthState.PasswordResetSent, viewModel.authState.value)
        verify { auth.sendPasswordResetEmail(email) }
    }
    
    @Test
    fun `resetPassword failure`() = runTest {
        // Given
        val email = "test@example.com"
        val errorMessage = "User not found"
        
        val mockTask = mockk<Task<Void>>()
        every { auth.sendPasswordResetEmail(email) } returns mockTask
        
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { mockTask.await() } throws Exception(errorMessage)
        
        // When
        viewModel.resetPassword(email)
        
        // Then
        assertTrue(viewModel.authState.value is AuthState.Error)
        assertEquals(errorMessage, (viewModel.authState.value as AuthState.Error).message)
    }
    

}