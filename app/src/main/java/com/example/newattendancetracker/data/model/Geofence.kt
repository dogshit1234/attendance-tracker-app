package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "geofences")
data class Geofence(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 100f, // Default radius in meters
    val companyId: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val description: String = ""
)