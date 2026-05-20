package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // Top, Bottom, Shoes, Jacket, Accessory, etc.
    val color: String,
    val season: String, // Summer, Winter, Spring, Autumn, All
    val formality: String, // Casual, Semi-Formal, Formal
    val pattern: String, // Solid, Striped, Plaid, Patterned
    val brand: String,
    val condition: String, // New, Good, Worn
    val imageUri: String, // local path, resource identifier, or base64 String
    val laundryStatus: String = "Clean" // Clean, Dirty
)

@Entity(tableName = "style_profiles")
data class StyleProfile(
    @PrimaryKey val id: Int = 1,
    val preferredStyle: String = "Casual", // Casual, Minimalist, Streetwear, Classic, Bohemian, Retro
    val bodyFit: String = "Regular", // Slim, Regular, Loose
    val likedColors: String = "Navy, White, Forest Green",
    val avoidedColors: String = "Neon Yellow",
    val workDressCode: String = "Smart Casual", // Casual, Smart Casual, Business Formal
    val culturalStyle: String = "Modern",
    val budget: String = "Moderate", // Budget, Moderate, Premium
    val modesty: String = "Standard", // Standard, High
    val fashionGoals: String = "Comfort & Confidence"
)

@Entity(tableName = "saved_outfits")
data class SavedOutfit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val topId: Int?,
    val bottomId: Int?,
    val shoesId: Int?,
    val jacketId: Int?,
    val accessoryId: Int?,
    val commentary: String,
    val weatherDescription: String,
    val eventName: String,
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val details: String,
    val eventType: String, // Work Meeting, Date Night, etc.
    val date: String,
    val time: String
)
