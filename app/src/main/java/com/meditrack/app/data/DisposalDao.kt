package com.meditrack.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DisposalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(disposal: DisposalEntity): Long

    @Query("SELECT * FROM disposals ORDER BY disposedDate DESC, timestamp DESC")
    fun getAllDisposals(): Flow<List<DisposalEntity>>

    @Query("SELECT * FROM disposals WHERE medicineId = :medicineId ORDER BY disposedDate DESC")
    fun getDisposalsForMedicine(medicineId: Long): Flow<List<DisposalEntity>>

    @Query("SELECT COUNT(*) FROM disposals")
    fun getTotalDisposalCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM disposals")
    fun getTotalQuantityDisposed(): Flow<Int?>

    @Query("SELECT * FROM disposals WHERE disposedDate BETWEEN :startDate AND :endDate ORDER BY disposedDate DESC")
    fun getDisposalsForRange(startDate: String, endDate: String): Flow<List<DisposalEntity>>

    @Query("DELETE FROM disposals WHERE medicineId = :medicineId")
    suspend fun deleteDisposalsForMedicine(medicineId: Long)
}
