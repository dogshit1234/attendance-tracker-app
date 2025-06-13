# Attendance Tracker App

A modern attendance tracking application built with Kotlin and Jetpack Compose.

## Recent Fixes

### Fixed Deprecated Icons
- Updated deprecated icons in multiple screens:
  - Replaced `Icons.Default.ArrowBack` with `Icons.AutoMirrored.Filled.ArrowBack` in LeaveManagementScreen and ReportsScreen
  - Replaced `Icons.Default.EventNote` with `Icons.AutoMirrored.Filled.EventNote` in TestNavigationScreen
  - Replaced `Icons.Default.Logout` with `Icons.AutoMirrored.Filled.Logout` in TestNavigationScreen

### Fixed Account Creation Issues
- Modified Firestore security rules to allow creating new user documents
- Improved loading state visibility in SignUpScreen
- Enhanced error handling in UserRepository
- Fixed navigation in MainActivity

## Features
- User authentication with Firebase
- Real-time attendance tracking
- Leave management
- Reports and analytics
- Admin panel for user management
- Secure check-in/check-out process

## Tech Stack
- Kotlin
- Jetpack Compose
- Firebase (Authentication, Firestore)
- Room Database
- Hilt for dependency injection
- MVVM architecture