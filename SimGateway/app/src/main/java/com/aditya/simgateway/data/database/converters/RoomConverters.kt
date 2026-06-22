package com.aditya.simgateway.data.database.converters

import androidx.room.TypeConverter
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.data.entity.MessageStatus

class RoomConverters {

    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)

    @TypeConverter
    fun fromEventCategory(category: EventCategory): String = category.name

    @TypeConverter
    fun toEventCategory(value: String): EventCategory = EventCategory.valueOf(value)
}
