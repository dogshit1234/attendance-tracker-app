package com.example.newattendancetracker.data.local.converter

import androidx.room.TypeConverter
import com.example.newattendancetracker.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }
    
    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }
    
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toAttendanceStatus(status: String): AttendanceStatus {
        return AttendanceStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromLeaveType(type: LeaveType): String {
        return type.name
    }
    
    @TypeConverter
    fun toLeaveType(type: String): LeaveType {
        return LeaveType.valueOf(type)
    }
    
    @TypeConverter
    fun fromLeaveStatus(status: LeaveStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toLeaveStatus(status: String): LeaveStatus {
        return LeaveStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromHalfDayPeriod(period: HalfDayPeriod?): String? {
        return period?.name
    }
    
    @TypeConverter
    fun toHalfDayPeriod(period: String?): HalfDayPeriod? {
        return period?.let { HalfDayPeriod.valueOf(it) }
    }
    
    @TypeConverter
    fun fromPayrollStatus(status: PayrollStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toPayrollStatus(status: String): PayrollStatus {
        return PayrollStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return locationString?.let { 
            gson.fromJson(it, Location::class.java)
        }
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromGeofenceList(value: List<Geofence>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toGeofenceList(value: String): List<Geofence> {
        val listType = object : TypeToken<List<Geofence>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromWorkingHours(workingHours: WorkingHours): String {
        return gson.toJson(workingHours)
    }
    
    @TypeConverter
    fun toWorkingHours(workingHoursString: String): WorkingHours {
        return gson.fromJson(workingHoursString, WorkingHours::class.java)
    }
    
    @TypeConverter
    fun fromCompanySettings(settings: CompanySettings): String {
        return gson.toJson(settings)
    }
    
    @TypeConverter
    fun toCompanySettings(settingsString: String): CompanySettings {
        return gson.fromJson(settingsString, CompanySettings::class.java)
    }
    
    @TypeConverter
    fun fromTaxDeductions(taxDeductions: TaxDeductions): String {
        return gson.toJson(taxDeductions)
    }
    
    @TypeConverter
    fun toTaxDeductions(taxDeductionsString: String): TaxDeductions {
        return gson.fromJson(taxDeductionsString, TaxDeductions::class.java)
    }
    
    @TypeConverter
    fun fromPayrollBreakdown(breakdown: PayrollBreakdown): String {
        return gson.toJson(breakdown)
    }
    
    @TypeConverter
    fun toPayrollBreakdown(breakdownString: String): PayrollBreakdown {
        return gson.fromJson(breakdownString, PayrollBreakdown::class.java)
    }
}