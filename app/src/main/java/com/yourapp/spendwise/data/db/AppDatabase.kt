package com.yourapp.spendwise.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TransactionEntity::class, PendingSmsEntity::class, SmsReviewEntity::class, TransactionCategoryAiEntity::class],
    version = 7,
    exportSchema = true
)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun transactionCategoryAiDao(): TransactionCategoryAiDao
    abstract fun pendingSmsDao(): PendingSmsDao
    abstract fun smsReviewDao(): SmsReviewDao

    class Converters {
        @TypeConverter
        fun fromTransactionType(value: TransactionType): String = value.name

        @TypeConverter
        fun toTransactionType(value: String): TransactionType {
            return runCatching { TransactionType.valueOf(value) }
                .getOrDefault(TransactionType.UNKNOWN)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN sourceSender TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE transactions ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE transactions ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE transactions ADD COLUMN verificationSource TEXT NOT NULL DEFAULT 'Prefilter'")
                database.execSQL("ALTER TABLE transactions ADD COLUMN aiReason TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE transactions ADD COLUMN paymentMode TEXT NOT NULL DEFAULT 'Other'")
                database.execSQL("ALTER TABLE pending_sms ADD COLUMN reviewEventId INTEGER")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sms_review_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sender TEXT NOT NULL,
                        body TEXT NOT NULL,
                        receivedAt INTEGER NOT NULL,
                        eventSource TEXT NOT NULL,
                        prefilterDecision TEXT NOT NULL,
                        previewAmount REAL NOT NULL,
                        previewType TEXT NOT NULL,
                        previewMerchant TEXT NOT NULL,
                        previewBank TEXT NOT NULL,
                        aiJson TEXT NOT NULL,
                        aiReason TEXT NOT NULL,
                        finalStatus TEXT NOT NULL,
                        transactionId INTEGER,
                        debugLog TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sms_review_events_eventSource ON sms_review_events(eventSource)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sms_review_events_finalStatus ON sms_review_events(finalStatus)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sms_review_events_receivedAt ON sms_review_events(receivedAt)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE sms_review_events ADD COLUMN aiEngine TEXT NOT NULL DEFAULT 'Gemini Nano'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN accountLabel TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN isIgnoredDuplicate INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN categoryDecisionSource TEXT NOT NULL DEFAULT 'RESOLVER'"
                )
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN categoryRefinementStatus TEXT NOT NULL DEFAULT 'NONE'"
                )
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN categoryRuleName TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "UPDATE transactions SET updatedAt = timestamp WHERE updatedAt = 0"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transaction_category_ai (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        transactionId INTEGER NOT NULL,
                        resolverCategory TEXT NOT NULL,
                        resolverSignalsJson TEXT NOT NULL,
                        currentCategory TEXT NOT NULL,
                        suggestedCategory TEXT NOT NULL,
                        confidence REAL NOT NULL,
                        reason TEXT NOT NULL,
                        model TEXT NOT NULL,
                        rawJson TEXT NOT NULL,
                        outcome TEXT NOT NULL,
                        outcomeDetail TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        finishedAt INTEGER NOT NULL,
                        keepCurrent INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_transaction_category_ai_transactionId ON transaction_category_ai(transactionId)"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN latitude REAL"
                )
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN longitude REAL"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendwise.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
