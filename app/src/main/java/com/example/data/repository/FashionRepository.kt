package com.example.data.repository

import com.example.data.dao.ClothingItemDao
import com.example.data.dao.SavedOutfitDao
import com.example.data.dao.StyleProfileDao
import com.example.data.dao.CalendarEventDao
import com.example.data.models.ClothingItem
import com.example.data.models.SavedOutfit
import com.example.data.models.StyleProfile
import com.example.data.models.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FashionRepository(
    private val clothingItemDao: ClothingItemDao,
    private val styleProfileDao: StyleProfileDao,
    private val savedOutfitDao: SavedOutfitDao,
    private val calendarEventDao: CalendarEventDao
) {
    val allClothingItems: Flow<List<ClothingItem>> = clothingItemDao.getAllClothingItems()
    val styleProfileFlow: Flow<StyleProfile?> = styleProfileDao.getStyleProfileFlow()
    val allSavedOutfits: Flow<List<SavedOutfit>> = savedOutfitDao.getAllSavedOutfits()
    val allEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()

    suspend fun insertClothingItem(item: ClothingItem): Long {
        return clothingItemDao.insertClothingItem(item)
    }

    suspend fun updateClothingItem(item: ClothingItem) {
        clothingItemDao.updateClothingItem(item)
    }

    suspend fun deleteClothingItem(item: ClothingItem) {
        clothingItemDao.deleteClothingItem(item)
    }

    suspend fun saveStyleProfile(profile: StyleProfile) {
        styleProfileDao.insertStyleProfile(profile)
    }

    suspend fun getStyleProfile(): StyleProfile {
        return styleProfileDao.getStyleProfile() ?: StyleProfile().also {
            styleProfileDao.insertStyleProfile(it)
        }
    }

    suspend fun insertEvent(event: CalendarEvent): Long {
        return calendarEventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        calendarEventDao.deleteEvent(event)
    }

    suspend fun insertSavedOutfit(outfit: SavedOutfit): Long {
        return savedOutfitDao.insertSavedOutfit(outfit)
    }

    suspend fun deleteSavedOutfit(outfit: SavedOutfit) {
        savedOutfitDao.deleteSavedOutfit(outfit)
    }

    suspend fun insertInitialClothingItems(items: List<ClothingItem>) {
        for (item in items) {
            clothingItemDao.insertClothingItem(item)
        }
    }

    suspend fun prepopulateIfEmpty() {
        val currentItems = clothingItemDao.getAllClothingItems().firstOrNull()
        if (currentItems.isNullOrEmpty()) {
            val defaults = listOf(
                ClothingItem(
                    name = "White Oxford Shirt",
                    type = "Top",
                    color = "White",
                    season = "All",
                    formality = "Semi-Formal",
                    pattern = "Solid",
                    brand = "Polo Ralph Lauren",
                    condition = "Good",
                    imageUri = "shirt_white"
                ),
                ClothingItem(
                    name = "Navy Chinos",
                    type = "Bottom",
                    color = "Navy Blue",
                    season = "All",
                    formality = "Semi-Formal",
                    pattern = "Solid",
                    brand = "Bonobos",
                    condition = "Good",
                    imageUri = "chino_navy"
                ),
                ClothingItem(
                    name = "Brown Leather Loafers",
                    type = "Shoes",
                    color = "Brown",
                    season = "All",
                    formality = "Semi-Formal",
                    pattern = "Solid",
                    brand = "Cole Haan",
                    condition = "Good",
                    imageUri = "loafer_brown"
                ),
                ClothingItem(
                    name = "Beige Cardigan",
                    type = "Jacket",
                    color = "Beige",
                    season = "Winter",
                    formality = "Casual",
                    pattern = "Solid",
                    brand = "Uniqlo",
                    condition = "Good",
                    imageUri = "sweater_beige"
                ),
                ClothingItem(
                    name = "Black Premium Crewneck",
                    type = "Top",
                    color = "Black",
                    season = "All",
                    formality = "Casual",
                    pattern = "Solid",
                    brand = "Everlane",
                    condition = "New",
                    imageUri = "crewneck_black"
                ),
                ClothingItem(
                    name = "Light Wash Denim",
                    type = "Bottom",
                    color = "Blue",
                    season = "All",
                    formality = "Casual",
                    pattern = "Solid",
                    brand = "Levi's",
                    condition = "Good",
                    imageUri = "denim_light"
                ),
                ClothingItem(
                    name = "White Leather Sneakers",
                    type = "Shoes",
                    color = "White",
                    season = "Summer",
                    formality = "Casual",
                    pattern = "Solid",
                    brand = "Common Projects",
                    condition = "Good",
                    imageUri = "sneaker_white"
                ),
                ClothingItem(
                    name = "Waterproof Active Windbreaker",
                    type = "Jacket",
                    color = "Navy Blue",
                    season = "Spring",
                    formality = "Casual",
                    pattern = "Solid",
                    brand = "Patagonia",
                    condition = "Good",
                    imageUri = "windbreaker_green"
                ),
                ClothingItem(
                    name = "Gold Round Chronograph",
                    type = "Accessory",
                    color = "Gold",
                    season = "All",
                    formality = "Formal",
                    pattern = "Solid",
                    brand = "Ray-Ban",
                    condition = "Good",
                    imageUri = "sunglasses_aviator"
                )
            )
            for (item in defaults) {
                clothingItemDao.insertClothingItem(item)
            }
        }

        val profile = styleProfileDao.getStyleProfile()
        if (profile == null) {
            styleProfileDao.insertStyleProfile(StyleProfile())
        }
    }
}
