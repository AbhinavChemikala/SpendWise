package com.yourapp.spendwise.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SmsReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SmsReviewEntity>): List<Long>

    @Query(
        """
        UPDATE sms_review_events
        SET finalStatus = :finalStatus,
            transactionId = :transactionId,
            aiJson = :aiJson,
            aiReason = :aiReason,
            aiEngine = :aiEngine,
            debugLog = :debugLog
        WHERE id = :eventId
        """
    )
    suspend fun updateOutcome(
        eventId: Long,
        finalStatus: String,
        transactionId: Long?,
        aiJson: String,
        aiReason: String,
        aiEngine: String = "Gemini Nano",
        debugLog: String
    ): Int

    @Query(
        """
        SELECT * FROM (
            SELECT * FROM sms_review_events
            WHERE finalStatus IN ('AI_CONFIRMED', 'AI_REJECTED', 'AI_FAILED')
            ORDER BY id DESC
            LIMIT 1000
        ) ORDER BY id ASC
        """
    )
    fun observeReviewCenter(): Flow<List<SmsReviewEntity>>

    @Query("SELECT * FROM sms_review_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SmsReviewEntity?

    @Query("SELECT * FROM sms_review_events ORDER BY id ASC")
    suspend fun getAll(): List<SmsReviewEntity>

    @Query("DELETE FROM sms_review_events")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM sms_review_events WHERE finalStatus = :status ORDER BY receivedAt DESC")
    suspend fun getByStatus(status: String): List<SmsReviewEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM sms_review_events 
            WHERE body = :body AND receivedAt = :receivedAt
        )
        """
    )
    fun exists(body: String, receivedAt: Long): Boolean

    @Query(
        """
        SELECT * FROM sms_review_events
        WHERE finalStatus IN ('SPAM_DISCARDED', 'AI_REJECTED')
          AND NOT (eventSource = 'EMAIL' AND finalStatus = 'SPAM_DISCARDED')
        ORDER BY receivedAt DESC
        LIMIT 150
        """
    )
    fun observeSpamInbox(): Flow<List<SmsReviewEntity>>

    @Query(
        """
        SELECT * FROM sms_review_events
        ORDER BY receivedAt DESC
        LIMIT 200
        """
    )
    fun observeDebugConsole(): Flow<List<SmsReviewEntity>>

    @Query(
        """
        SELECT * FROM sms_review_events
        WHERE eventSource = :eventSource
        ORDER BY receivedAt DESC
        LIMIT 200
        """
    )
    fun observeSourceEvents(eventSource: String): Flow<List<SmsReviewEntity>>
}
