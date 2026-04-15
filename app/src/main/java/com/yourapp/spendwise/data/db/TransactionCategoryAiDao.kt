package com.yourapp.spendwise.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionCategoryAiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: TransactionCategoryAiEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<TransactionCategoryAiEntity>): List<Long>

    @Query(
        """
        SELECT * FROM transaction_category_ai
        WHERE transactionId = :transactionId
        LIMIT 1
        """
    )
    fun observeByTransactionId(transactionId: Long): Flow<TransactionCategoryAiEntity?>

    @Query(
        """
        SELECT * FROM transaction_category_ai
        WHERE transactionId = :transactionId
        LIMIT 1
        """
    )
    suspend fun getByTransactionId(transactionId: Long): TransactionCategoryAiEntity?

    @Query("DELETE FROM transaction_category_ai WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: Long): Int

    @Query("SELECT * FROM transaction_category_ai ORDER BY id ASC")
    suspend fun getAll(): List<TransactionCategoryAiEntity>

    @Query("DELETE FROM transaction_category_ai")
    suspend fun deleteAll(): Int
}
