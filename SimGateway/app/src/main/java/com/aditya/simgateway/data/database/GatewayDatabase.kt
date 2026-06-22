package com.aditya.simgateway.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
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
            return Room.databaseBuilder(
                context = context,
                klass = GatewayDatabase::class.java,
                name = DATABASE_NAME
            ).build()
        }
    }
}
