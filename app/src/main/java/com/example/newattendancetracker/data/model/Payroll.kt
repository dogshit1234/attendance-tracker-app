package com.example.newattendancetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "payroll")
data class Payroll(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val month: Int = 1,
    val year: Int = 2024,
    val basicSalary: Double = 0.0,
    val overtimePay: Double = 0.0,
    val bonuses: Double = 0.0,
    val allowances: Double = 0.0,
    val deductions: Double = 0.0,
    val grossSalary: Double = 0.0,
    val netSalary: Double = 0.0,
    val totalWorkingDays: Int = 0,
    val actualWorkingDays: Int = 0,
    val totalHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val leaveDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val status: PayrollStatus = PayrollStatus.DRAFT,
    val generatedAt: Date = Date(),
    val processedAt: Date? = null,
    val processedBy: String? = null,
    val paymentDate: Date? = null,
    val paymentMethod: String = "",
    val paymentReference: String = "",
    val taxDeductions: TaxDeductions = TaxDeductions(),
    val breakdown: PayrollBreakdown = PayrollBreakdown()
)

enum class PayrollStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    PROCESSED,
    PAID,
    CANCELLED
}

data class TaxDeductions(
    val incomeTax: Double = 0.0,
    val socialSecurity: Double = 0.0,
    val healthInsurance: Double = 0.0,
    val providentFund: Double = 0.0,
    val professionalTax: Double = 0.0,
    val other: Double = 0.0
)

data class PayrollBreakdown(
    val regularHours: Double = 0.0,
    val regularPay: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val overtimePay: Double = 0.0,
    val holidayHours: Double = 0.0,
    val holidayPay: Double = 0.0,
    val nightShiftHours: Double = 0.0,
    val nightShiftPay: Double = 0.0,
    val weekendHours: Double = 0.0,
    val weekendPay: Double = 0.0
)

data class PayrollSummary(
    val month: Int,
    val year: Int,
    val totalEmployees: Int,
    val totalGrossSalary: Double,
    val totalNetSalary: Double,
    val totalDeductions: Double,
    val totalTaxes: Double,
    val totalOvertimePay: Double,
    val averageSalary: Double,
    val processedCount: Int,
    val pendingCount: Int
)