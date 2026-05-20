package com.example.data.dao

import androidx.room.*
import com.example.data.models.ClothingItem
import com.example.data.models.StyleProfile
import com.example.data.models.SavedOutfit
import com.example.data.models.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    @Query("SELECT * FROM clothing_items ORDER BY id DESC")
    fun getAllClothingItems(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    suspend fun getClothingItemById(id: Int): ClothingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItem(item: ClothingItem): Long

    @Update
    suspend fun updateClothingItem(item: ClothingItem)

    @Delete
    suspend fun deleteClothingItem(item: ClothingItem)

    @Query("DELETE FROM clothing_items")
    suspend fun deleteAllClothingItems()
}

@Dao
interface StyleProfileDao {
    @Query("SELECT * FROM style_profiles WHERE id = 1 LIMIT 1")
    fun getStyleProfileFlow(): Flow<StyleProfile?>

    @Query("SELECT * FROM style_profiles WHERE id = 1 LIMIT 1")
    suspend fun getStyleProfile(): StyleProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyleProfile(profile: StyleProfile)
}

@Dao
interface SavedOutfitDao {
    @Query("SELECT * FROM saved_outfits ORDER BY timestamp DESC")
    fun getAllSavedOutfits(): Flow<List<SavedOutfit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedOutfit(outfit: SavedOutfit): Long

    @Delete
    suspend fun deleteSavedOutfit(outfit: SavedOutfit)
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY id DESC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}
