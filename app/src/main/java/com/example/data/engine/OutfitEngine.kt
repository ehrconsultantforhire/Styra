package com.example.data.engine

import com.example.data.models.ClothingItem
import com.example.data.models.StyleProfile
import com.example.data.models.SavedOutfit
import java.util.Locale

object OutfitEngine {

    /**
     * Algorithmic local recommendations when Gemini is unavailable or key is missing.
     * Selects matching pieces depending on event, current weather temperature, and style assessment.
     */
    fun recommendOutfitLocally(
        items: List<ClothingItem>,
        profile: StyleProfile,
        weatherTemp: Int,
        weatherDesc: String,
        eventName: String
    ): SavedOutfit {
        if (items.isEmpty()) {
            return SavedOutfit(
                title = "Please add items to your closet",
                topId = null,
                bottomId = null,
                shoesId = null,
                jacketId = null,
                accessoryId = null,
                commentary = "Styra needs garments in your digital closet to recommend outfits. Tap the '+' tab to add your clothes!",
                weatherDescription = "$weatherTemp°F, $weatherDesc",
                eventName = eventName
            )
        }

        // 1. Identify style formality needed for event
        val requiredFormality = when (eventName.lowercase(Locale.ROOT)) {
            "work", "interview", "networking event" -> "Semi-Formal"
            "wedding", "church" -> "Formal"
            else -> "Casual"
        }

        // 2. Identify season suitability
        val preferredSeason = when {
            weatherTemp < 55 -> "Winter"
            weatherTemp > 75 -> "Summer"
            else -> "All"
        }

        // 3. Filter avoided colors
        val avoided = profile.avoidedColors.split(",").map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotEmpty() }
        val filteredItems = items.filter { item ->
            val colorLower = item.color.lowercase(Locale.ROOT)
            avoided.none { colorLower.contains(it) }
        }

        val pool = if (filteredItems.isEmpty()) items else filteredItems

        // 4. Sort clothes based on preferred formality fit and select key components
        val tops = pool.filter { it.type.equals("Top", ignoreCase = true) }
        val bottoms = pool.filter { it.type.equals("Bottom", ignoreCase = true) }
        val shoes = pool.filter { it.type.equals("Shoes", ignoreCase = true) }
        val jackets = pool.filter { it.type.equals("Jacket", ignoreCase = true) }
        val accessories = pool.filter { it.type.equals("Accessory", ignoreCase = true) }

        // Find best top
        val selectedTop = tops.find { it.formality.equals(requiredFormality, ignoreCase = true) }
            ?: tops.find { it.season.equals(preferredSeason, ignoreCase = true) || it.season.equals("All", ignoreCase = true) }
            ?: tops.firstOrNull()

        // Find best bottom to complement
        val selectedBottom = bottoms.filter {
            // Try matching color families or avoiding same-color top-bottom overlap unless intentional
            if (selectedTop != null) !it.color.equals(selectedTop.color, ignoreCase = true) else true
        }.find { it.formality.equals(requiredFormality, ignoreCase = true) }
            ?: bottoms.find { it.season.equals(preferredSeason, ignoreCase = true) || it.season.equals("All", ignoreCase = true) }
            ?: bottoms.firstOrNull()

        // Find best shoes
        val selectedShoes = shoes.find { it.formality.equals(requiredFormality, ignoreCase = true) }
            ?: shoes.firstOrNull()

        // Determine if jacket is needed (< 68°F or raining/wet)
        val needsJacket = weatherTemp < 68 || weatherDesc.lowercase(Locale.ROOT).contains("rain") || weatherDesc.lowercase(Locale.ROOT).contains("snow")
        val selectedJacket = if (needsJacket) {
            jackets.find { it.season.equals(preferredSeason, ignoreCase = true) || it.season.equals("All", ignoreCase = true) } ?: jackets.firstOrNull()
        } else {
            null
        }

        // Accessories
        val selectedAccessory = accessories.firstOrNull()

        // 5. Generate descriptive text/commentary
        val topStr = selectedTop?.name ?: "a neat top"
        val bottomStr = selectedBottom?.name ?: "matching pants"
        val shoesStr = selectedShoes?.name ?: "comfortable footwear"
        val jacketStr = selectedJacket?.let { " with the ${it.name}" } ?: ""
        val weatherWarning = if (weatherDesc.lowercase(Locale.ROOT).contains("rain")) ". Bring an umbrella!" else "."

        val commentary = "For $eventName on this $weatherTemp°F day ($weatherDesc), we styled a $requiredFormality look using your elegant $topStr paired beautifully with the $bottomStr and finished off with $shoesStr$jacketStr$weatherWarning This combo respects your preferred ${profile.preferredStyle} profile and ${profile.bodyFit} fit preferences."

        val title = when (requiredFormality) {
            "Formal" -> "${profile.preferredStyle} Elegance"
            "Semi-Formal" -> "Smart ${profile.preferredStyle} Look"
            else -> "Effortless ${profile.preferredStyle}"
        }

        return SavedOutfit(
            title = title,
            topId = selectedTop?.id,
            bottomId = selectedBottom?.id,
            shoesId = selectedShoes?.id,
            jacketId = selectedJacket?.id,
            accessoryId = selectedAccessory?.id,
            commentary = commentary,
            weatherDescription = "$weatherTemp°F, $weatherDesc",
            eventName = eventName
        )
    }
}
