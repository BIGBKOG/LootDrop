package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.PlayStatus

class Converters {
    @TypeConverter
    fun fromPlayStatus(status: PlayStatus?): String {
        return (status ?: PlayStatus.NOT_STARTED).name
    }

    @TypeConverter
    fun toPlayStatus(value: String?): PlayStatus {
        return PlayStatus.fromString(value)
    }
}
