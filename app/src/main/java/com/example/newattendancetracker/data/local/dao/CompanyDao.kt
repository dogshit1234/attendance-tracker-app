package com.example.newattendancetracker.data.local.dao

import androidx.room.*
import com.example.newattendancetracker.data.model.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    
    @Query("SELECT * FROM companies WHERE id = :companyId")
    suspend fun getCompanyById(companyId: String): Company?
    
    @Query("SELECT * FROM companies WHERE id = :companyId")
    fun getCompanyByIdFlow(companyId: String): Flow<Company?>
    
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<Company>>
    
    @Query("SELECT * FROM companies WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchCompanies(searchQuery: String): Flow<List<Company>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<Company>)
    
    @Update
    suspend fun updateCompany(company: Company)
    
    @Delete
    suspend fun deleteCompany(company: Company)
    
    @Query("DELETE FROM companies WHERE id = :companyId")
    suspend fun deleteCompanyById(companyId: String)
    
    @Query("DELETE FROM companies")
    suspend fun deleteAllCompanies()
}