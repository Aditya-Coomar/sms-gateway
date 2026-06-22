package com.aditya.simgateway.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aditya.simgateway.data.dao.DeviceConfigDao
import com.aditya.simgateway.data.dao.EventLogDao
import com.aditya.simgateway.data.dao.MessageDao
import com.aditya.simgateway.data.database.converters.RoomConverters
import com.aditya.simgateway.data.entity.DeviceConfigEntity
import com.aditya.simgateway.data.entity.EventLogEntity
import com.aditya.simgateway.data.entity.MessageEntity

@Database(
    entities = [
        DeviceConfigEntity::class,
        MessageEntity::class,
        EventLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class GatewayDatabase : RoomDatabase() {

    abstract fun deviceConfigDao(): DeviceConfigDao

    abstract fun messageDao(): MessageDao

    abstract fun eventLogDao(): EventLogDao

    companion object {
        private const val DATABASE_NAME = "gateway.db"

        fun create(context: Context): GatewayDatabase {
            return Room.databaseBuilder(context, GatewayDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE device_config
                    ADD COLUMN defaultSimSlot INTEGER
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE device_config
                    ADD COLUMN maxRetryCount INTEGER NOT NULL DEFAULT 4
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE device_config
                    ADD COLUMN deliveryReportsEnabled INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS messages_new (
                        id TEXT NOT NULL,
                        recipient TEXT NOT NULL,
                        body TEXT NOT NULL,
                        simSlot INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        retryCount INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        sentAt INTEGER,
                        deliveredAt INTEGER,
                        failureReason TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO messages_new (
                        id,
                        recipient,
                        body,
                        simSlot,
                        status,
                        retryCount,
                        createdAt,
                        sentAt,
                        deliveredAt,
                        failureReason
                    )
                    SELECT
                        id,
                        recipient,
                        body,
                        simSlot,
                        CASE
                            WHEN status = 'PENDING' THEN 'QUEUED'
                            ELSE status
                        END,
                        0,
                        createdAt,
                        sentAt,
                        deliveredAt,
                        errorMessage
                    FROM messages
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE messages")
                db.execSQL("ALTER TABLE messages_new RENAME TO messages")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_messages_createdAt ON messages(createdAt)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_messages_status ON messages(status)"
                )
            }
        }
    }
}
