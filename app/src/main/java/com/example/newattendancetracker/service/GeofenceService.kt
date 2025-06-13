package com.example.newattendancetracker.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.newattendancetracker.data.model.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceService : Service() {
    
    @Inject
    lateinit var geofencingClient: GeofencingClient
    
    private lateinit var geofencePendingIntent: PendingIntent
    
    override fun onCreate() {
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    fun addGeofences(geofences: List<Geofence>) {
        val geofenceList = geofences.map { geofence ->
            com.google.android.gms.location.Geofence.Builder()
                .setRequestId(geofence.id)
                .setCircularRegion(
                    geofence.latitude,
                    geofence.longitude,
                    geofence.radius
                )
                .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or
                    com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
        }
        
        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
        
        try {
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
        } catch (securityException: SecurityException) {
            // Handle permission error
        }
    }
    
    fun removeGeofences(geofenceIds: List<String>) {
        geofencingClient.removeGeofences(geofenceIds)
    }
    
    private fun getGeofencePendingIntent(): PendingIntent {
        if (::geofencePendingIntent.isInitialized) {
            return geofencePendingIntent
        }
        
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return geofencePendingIntent
    }
}