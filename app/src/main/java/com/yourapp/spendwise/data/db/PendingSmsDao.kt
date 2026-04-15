package com.yourapp.spendwise.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingSmsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pendingSms: PendingSmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pendingSms: List<PendingSmsEntity>): List<Long>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM pending_sms
            WHERE sender = :sender AND body = :body AND receivedAt = :receivedAt
        )
        """
    )
    suspend fun exists(sender: String, body: String, receivedAt: Long): Boolean

    @Query(
        """
        SELECT * FROM pending_sms
        ORDER BY receivedAt ASC
        """
    )
    suspend fun getAll(): List<PendingSmsEntity>

    @Query("DELETE FROM pending_sms WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM pending_sms")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM pending_sms")
    suspend fun getPendingCount(): Int
}
