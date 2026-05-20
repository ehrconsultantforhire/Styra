package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ClothingItemDao
import com.example.data.dao.SavedOutfitDao
import com.example.data.dao.StyleProfileDao
import com.example.data.dao.CalendarEventDao
import com.example.data.models.ClothingItem
import com.example.data.models.SavedOutfit
import com.example.data.models.StyleProfile
import com.example.data.models.CalendarEvent

@Database(
    entities = [ClothingItem::class, StyleProfile::class, SavedOutfit::class, CalendarEvent::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun styleProfileDao(): StyleProfileDao
    abstract fun savedOutfitDao(): SavedOutfitDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styra_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
