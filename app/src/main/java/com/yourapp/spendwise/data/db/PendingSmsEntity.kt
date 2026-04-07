package com.yourapp.spendwise.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sms")
data class PendingSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val reviewEventId: Long? = null
)
