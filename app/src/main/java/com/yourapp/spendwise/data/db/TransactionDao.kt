package com.yourapp.spendwise.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MonthlySummary(
    val totalSpent: Double = 0.0,
    val totalReceived: Double = 0.0
)

data class CategoryTotal(
    val category: String,
    val totalAmount: Double
)

data class TransactionCount(
    val count: Int = 0
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Update
    suspend fun updateAll(transactions: List<TransactionEntity>): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll(): Int

    @Query("UPDATE transactions SET isIgnoredDuplicate = 1 WHERE id = :id")
    suspend fun ignoreDuplicate(id: Long): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM transactions
            WHERE rawSms = :rawSms AND timestamp = :timestamp
        )
        """
    )
    suspend fun exists(rawSms: String, timestamp: Long): Boolean

    @Query(
        """
        SELECT * FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        ORDER BY timestamp DESC
        """
    )
    fun getTransactionsByMonth(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        ORDER BY timestamp DESC
        """
    )
    suspend fun getTransactionsList(startMs: Long, endMs: Long): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY timestamp DESC
        """
    )
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE 0 END), 0) AS totalSpent,
            COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END), 0) AS totalReceived
        FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        """
    )
    suspend fun getMonthlySummary(startMs: Long, endMs: Long): MonthlySummary

    @Query(
        """
        SELECT category, COALESCE(SUM(amount), 0) AS totalAmount
        FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
          AND type = 'DEBIT'
        GROUP BY category
        ORDER BY totalAmount DESC
        LIMIT 5
        """
    )
    suspend fun getTopCategories(startMs: Long, endMs: Long): List<CategoryTotal>

    @Query(
        """
        SELECT COUNT(*) AS count
        FROM transactions
        WHERE timestamp BETWEEN :startMs AND :endMs
        """
    )
    suspend fun getTransactionCount(startMs: Long, endMs: Long): TransactionCount
}
