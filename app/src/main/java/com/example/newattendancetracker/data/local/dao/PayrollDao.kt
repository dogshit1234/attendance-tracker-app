package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.Payroll
import com.example.newattendancetracker.data.model.PayrollStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollDao {
    
    @Query("SELECT * FROM payroll WHERE id = :payrollId")
    suspend fun getPayrollById(payrollId: String): Payroll?
    
    @Query("SELECT * FROM payroll WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getPayrollByUser(userId: String): Flow<List<Payroll>>
    
    @Query("SELECT * FROM payroll WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getPayrollByUserAndMonth(userId: String, month: Int, year: Int): Payroll?
    
    @Query("SELECT * FROM payroll WHERE month = :month AND year = :year ORDER BY userId ASC")
    fun getPayrollByMonth(month: Int, year: Int): Flow<List<Payroll>>
    
    @Query("SELECT * FROM payroll WHERE status = :status ORDER BY year DESC, month DESC")
    fun getPayrollByStatus(status: PayrollStatus): Flow<List<Payroll>>
    
    @Query("SELECT * FROM payroll WHERE year = :year ORDER BY month DESC, userId ASC")
    fun getPayrollByYear(year: Int): Flow<List<Payroll>>
    
    @Query("SELECT SUM(grossSalary) FROM payroll WHERE month = :month AND year = :year AND status = 'PROCESSED'")
    suspend fun getTotalGrossSalaryByMonth(month: Int, year: Int): Double?
    
    @Query("SELECT SUM(netSalary) FROM payroll WHERE month = :month AND year = :year AND status = 'PROCESSED'")
    suspend fun getTotalNetSalaryByMonth(month: Int, year: Int): Double?
    
    @Query("SELECT COUNT(*) FROM payroll WHERE month = :month AND year = :year")
    suspend fun getPayrollCountByMonth(month: Int, year: Int): Int
    
    @Query("SELECT COUNT(*) FROM payroll WHERE status = :status")
    suspend fun getPayrollCountByStatus(status: PayrollStatus): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: Payroll)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrolls(payrolls: List<Payroll>)
    
    @Update
    suspend fun updatePayroll(payroll: Payroll)
    
    @Delete
    suspend fun deletePayroll(payroll: Payroll)
    
    @Query("DELETE FROM payroll WHERE id = :payrollId")
    suspend fun deletePayrollById(payrollId: String)
    
    @Query("DELETE FROM payroll WHERE userId = :userId")
    suspend fun deletePayrollByUser(userId: String)
    
    @Query("DELETE FROM payroll")
    suspend fun deleteAllPayroll()
}