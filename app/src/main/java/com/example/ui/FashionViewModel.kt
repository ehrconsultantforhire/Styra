package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GeminiService
import com.example.api.TaggingResult
import com.example.data.db.AppDatabase
import com.example.data.models.ClothingItem
import com.example.data.models.StyleProfile
import com.example.data.models.SavedOutfit
import com.example.data.models.CalendarEvent
import com.example.data.repository.FashionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class MissingItemRec(
    val name: String,
    val type: String,
    val whyNeeded: String,
    val searchKeyword: String,
    val priceRange: String,
    val altColors: List<String>,
    val suggestedBrands: List<String>
)

data class WardrobeAnalysisResult(
    val completenessScore: Int,
    val feedback: String,
    val recommendations: List<MissingItemRec>,
    val countsByType: Map<String, Int>
)

class FashionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FashionRepository(
        database.clothingItemDao(),
        database.styleProfileDao(),
        database.savedOutfitDao(),
        database.calendarEventDao()
    )

    // Closet items, style profile, events, and saved outfits
    val clothingItems: StateFlow<List<ClothingItem>> = repository.allClothingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val styleProfile: StateFlow<StyleProfile> = repository.styleProfileFlow
        .map { it ?: StyleProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StyleProfile())

    val savedOutfits: StateFlow<List<SavedOutfit>> = repository.allSavedOutfits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _isCustomizingProfile = MutableStateFlow(false)
    val isCustomizingProfile = _isCustomizingProfile.asStateFlow()

    // Current simulator settings
    private val _currentEvent = MutableStateFlow("Work Meeting")
    val currentEvent = _currentEvent.asStateFlow()

    // Weather inputs
    private val _temperature = MutableStateFlow("68°F")
    val temperature = _temperature.asStateFlow()

    private val _weatherCondition = MutableStateFlow("Partly Cloudy")
    val weatherCondition = _weatherCondition.asStateFlow()

    private val _humidity = MutableStateFlow("42%")
    val humidity = _humidity.asStateFlow()

    private val _windSpeed = MutableStateFlow("8 mph")
    val windSpeed = _windSpeed.asStateFlow()

    private val _uvIndex = MutableStateFlow("Moderate")
    val uvIndex = _uvIndex.asStateFlow()

    // Outfit Proposal Results
    private val _proposedOutfitTitle = MutableStateFlow("Modern Corporate")
    val proposedOutfitTitle = _proposedOutfitTitle.asStateFlow()

    private val _proposedCommentary = MutableStateFlow(
        "A premium choice featuring a tailored layout. The bone neutral palette and olive accents deliver professional poise for standard business smart casual meetings."
    )
    val proposedCommentary = _proposedCommentary.asStateFlow()

    private val _proposedTop = MutableStateFlow<ClothingItem?>(null)
    val proposedTop = _proposedTop.asStateFlow()

    private val _proposedBottom = MutableStateFlow<ClothingItem?>(null)
    val proposedBottom = _proposedBottom.asStateFlow()

    private val _proposedShoes = MutableStateFlow<ClothingItem?>(null)
    val proposedShoes = _proposedShoes.asStateFlow()

    private val _proposedJacket = MutableStateFlow<ClothingItem?>(null)
    val proposedJacket = _proposedJacket.asStateFlow()

    private val _proposedAccessory = MutableStateFlow<ClothingItem?>(null)
    val proposedAccessory = _proposedAccessory.asStateFlow()

    private val _isGeneratingOutfit = MutableStateFlow(false)
    val isGeneratingOutfit = _isGeneratingOutfit.asStateFlow()

    // Gemini Tagging States (temporary slot during cloth addition)
    private val _aiTaggingResult = MutableStateFlow<TaggingResult?>(null)
    val aiTaggingResult = _aiTaggingResult.asStateFlow()

    private val _isAiTagging = MutableStateFlow(false)
    val isAiTagging = _isAiTagging.asStateFlow()

    // Wardrobe Gap Analysis states
    private val _isAnalyzingWardrobe = MutableStateFlow(false)
    val isAnalyzingWardrobe = _isAnalyzingWardrobe.asStateFlow()

    private val _wardrobeAnalysis = MutableStateFlow<WardrobeAnalysisResult?>(null)
    val wardrobeAnalysis = _wardrobeAnalysis.asStateFlow()

    init {
        // Pre-populate some fashionable default closet items if empty
        viewModelScope.launch {
            repository.allClothingItems.first().let { items ->
                if (items.isEmpty()) {
                    val defaults = listOf(
                        ClothingItem(
                            name = "White Oxford Shirt",
                            type = "Top",
                            color = "White",
                            season = "All",
                            formality = "Semi-Formal",
                            pattern = "Solid",
                            brand = "Everlane",
                            condition = "New",
                            imageUri = "default_shirt"
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
                            imageUri = "default_pants"
                        ),
                        ClothingItem(
                            name = "Classic Crimson Tee",
                            type = "Top",
                            color = "Crimson Red",
                            season = "Summer",
                            formality = "Casual",
                            pattern = "Solid",
                            brand = "Zara",
                            condition = "Good",
                            imageUri = "default_red_tee"
                        ),
                        ClothingItem(
                            name = "Sleek Black Leggings",
                            type = "Bottom",
                            color = "Black",
                            season = "All",
                            formality = "Casual",
                            pattern = "Solid",
                            brand = "Lululemon",
                            condition = "Good",
                            imageUri = "default_leggings"
                        ),
                        ClothingItem(
                            name = "Brown Leather Loafers",
                            type = "Shoes",
                            color = "Brown",
                            season = "All",
                            formality = "Semi-Formal",
                            pattern = "Solid",
                            brand = "Allen Edmonds",
                            condition = "Good",
                            imageUri = "default_loafers"
                        ),
                        ClothingItem(
                            name = "Beige Cardigan Jacket",
                            type = "Jacket",
                            color = "Beige",
                            season = "Autumn",
                            formality = "Casual",
                            pattern = "Solid",
                            brand = "Uniqlo",
                            condition = "Good",
                            imageUri = "default_cardigan"
                        ),
                        ClothingItem(
                            name = "Waterproof Active Windbreaker",
                            type = "Jacket",
                            color = "Navy Blue",
                            season = "Winter",
                            formality = "Casual",
                            pattern = "Solid",
                            brand = "Patagonia",
                            condition = "New",
                            imageUri = "default_windbreaker"
                        ),
                        ClothingItem(
                            name = "Gold Round Chronograph",
                            type = "Accessory",
                            color = "Gold",
                            season = "All",
                            formality = "Formal",
                            pattern = "Solid",
                            brand = "Seiko",
                            condition = "New",
                            imageUri = "default_watch"
                        )
                    )
                    repository.insertInitialClothingItems(defaults)
                }
            }

            // Check style profile
            repository.getStyleProfile()

            // Pre-populate some initial calendar events
            repository.allEvents.first().let { events ->
                if (events.isEmpty()) {
                    repository.insertEvent(
                        CalendarEvent(
                            title = "Project Pitch Discussion",
                            details = "Pitch deck reviews with client executives.",
                            eventType = "Work Meeting",
                            date = "May 21, 2026",
                            time = "10:30 AM"
                        )
                    )
                    repository.insertEvent(
                        CalendarEvent(
                            title = "Anniversary Dinner",
                            details = "Romantic reservation at Bistro Rustique.",
                            eventType = "Date Night",
                            date = "May 22, 2026",
                            time = "07:30 PM"
                        )
                    )
                }
            }

            // Generate initial outfit
            regenerateLocalOutfitProposal()
            runWardrobeAnalysis()
        }
    }

    // Update profile
    fun updateStyleProfile(profile: StyleProfile) {
        viewModelScope.launch {
            repository.saveStyleProfile(profile)
            regenerateOutfitProposal()
        }
    }

    // Closet actions
    fun addClothingItem(
        name: String,
        type: String,
        color: String,
        season: String,
        formality: String,
        pattern: String,
        brand: String,
        condition: String,
        imageUri: String
    ) {
        viewModelScope.launch {
            val item = ClothingItem(
                name = name,
                type = type,
                color = color,
                season = season,
                formality = formality,
                pattern = pattern,
                brand = brand,
                condition = condition,
                imageUri = imageUri
            )
            repository.insertClothingItem(item)
            regenerateOutfitProposal()
        }
    }

    fun deleteClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.deleteClothingItem(item)
            regenerateOutfitProposal()
        }
    }

    fun changeLaundryStatus(item: ClothingItem, status: String) {
        viewModelScope.launch {
            val updated = item.copy(laundryStatus = status)
            repository.updateClothingItem(updated)
            regenerateOutfitProposal()
        }
    }

    // Calendar Action items
    fun addCalendarEvent(title: String, type: String, details: String, date: String, time: String) {
        viewModelScope.launch {
            val newEvent = CalendarEvent(
                title = title,
                eventType = type,
                details = details,
                date = date,
                time = time
            )
            repository.insertEvent(newEvent)
        }
    }

    fun removeCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // Favorites & Ratings
    fun saveProposedAsFavorite() {
        viewModelScope.launch {
            val outfit = SavedOutfit(
                title = _proposedOutfitTitle.value,
                topId = _proposedTop.value?.id,
                bottomId = _proposedBottom.value?.id,
                shoesId = _proposedShoes.value?.id,
                jacketId = _proposedJacket.value?.id,
                accessoryId = _proposedAccessory.value?.id,
                commentary = _proposedCommentary.value,
                weatherDescription = "${_temperature.value} & ${_weatherCondition.value}",
                eventName = _currentEvent.value,
                rating = 5.0f
            )
            repository.insertSavedOutfit(outfit)
        }
    }

    fun deleteSavedOutfit(outfit: SavedOutfit) {
        viewModelScope.launch {
            repository.deleteSavedOutfit(outfit)
        }
    }

    fun changeWeatherSim(temp: String, cond: String, hum: String, wind: String, uv: String) {
        _temperature.value = temp
        _weatherCondition.value = cond
        _humidity.value = hum
        _windSpeed.value = wind
        _uvIndex.value = uv
        regenerateOutfitProposal()
    }

    fun changeEventSim(event: String) {
        _currentEvent.value = event
        regenerateOutfitProposal()
    }

    fun analyzeNewClothingPhoto(context: Context, uri: Uri) {
        _isAiTagging.value = true
        _aiTaggingResult.value = null
        viewModelScope.launch {
            try {
                val result = GeminiService.analyzeClothingImage(context, uri)
                _aiTaggingResult.value = result
            } catch (e: Exception) {
                Log.e("FashionViewModel", "AI Image tagging failed", e)
                _aiTaggingResult.value = TaggingResult(
                    success = false,
                    error = e.localizedMessage ?: "Unknown tagging exception"
                )
            } finally {
                _isAiTagging.value = false
            }
        }
    }

    fun clearAiTagging() {
        _aiTaggingResult.value = null
    }

    fun regenerateOutfitProposal() {
        runWardrobeAnalysis()
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasKey) {
            regenerateLocalOutfitProposal()
            return
        }

        _isGeneratingOutfit.value = true
        viewModelScope.launch {
            try {
                val items = clothingItems.value
                val profile = styleProfile.value

                val availableItemsFormatted = JSONArray().apply {
                    items.filter { it.laundryStatus == "Clean" }.forEach {
                        put(JSONObject().apply {
                            put("id", it.id)
                            put("name", it.name)
                            put("type", it.type)
                            put("color", it.color)
                            put("season", it.season)
                            put("formality", it.formality)
                            put("pattern", it.pattern)
                            put("condition", it.condition)
                        })
                    }
                }.toString()

                val profileFormatted = JSONObject().apply {
                    put("preferredStyle", profile.preferredStyle)
                    put("bodyFit", profile.bodyFit)
                    put("likedColors", profile.likedColors)
                    put("avoidedColors", profile.avoidedColors)
                    put("workDressCode", profile.workDressCode)
                }.toString()

                val result = GeminiService.getOutfitRecommendation(
                    apiKey = apiKey,
                    temperature = _temperature.value,
                    condition = _weatherCondition.value,
                    eventName = _currentEvent.value,
                    userProfile = profileFormatted,
                    availableItemsJson = availableItemsFormatted
                )

                _proposedOutfitTitle.value = result.first
                _proposedCommentary.value = result.second

                val matchedIds = result.third.mapNotNull { it.toIntOrNull() }
                _proposedTop.value = items.find { it.id in matchedIds && it.type == "Top" } ?: items.find { it.type == "Top" }
                _proposedBottom.value = items.find { it.id in matchedIds && it.type == "Bottom" } ?: items.find { it.type == "Bottom" }
                _proposedShoes.value = items.find { it.id in matchedIds && it.type == "Shoes" } ?: items.find { it.type == "Shoes" }
                _proposedJacket.value = items.find { it.id in matchedIds && it.type == "Jacket" }
                _proposedAccessory.value = items.find { it.id in matchedIds && it.type == "Accessory" }

            } catch (e: Exception) {
                Log.e("FashionViewModel", "AI Outfit generation failed. Doing local matchmaking.", e)
                regenerateLocalOutfitProposal()
            } finally {
                _isGeneratingOutfit.value = false
            }
        }
    }

    private fun regenerateLocalOutfitProposal() {
        val items = clothingItems.value
        if (items.isEmpty()) return

        val cleanItems = items.filter { it.laundryStatus == "Clean" }.ifEmpty { items }
        val profile = styleProfile.value

        val isFormalEvent = _currentEvent.value.lowercase().let {
            it.contains("work") || it.contains("interview") || it.contains("wedding") || it.contains("church") || it.contains("networking")
        }
        val isAthletic = _currentEvent.value.lowercase().contains("gym")
        val targetFormality = if (isFormalEvent) "Semi-Formal" else "Casual"

        val tops = cleanItems.filter { it.type == "Top" }
        val bottoms = cleanItems.filter { it.type == "Bottom" }
        val shoes = cleanItems.filter { it.type == "Shoes" }
        val jackets = cleanItems.filter { it.type == "Jacket" }
        val accessories = cleanItems.filter { it.type == "Accessory" }

        val bestTop = tops.find {
            (if (isAthletic) it.name.lowercase().contains("athletic") else true) &&
                    (it.formality == targetFormality || it.formality == "Semi-Formal")
        } ?: tops.find { it.formality == targetFormality } ?: tops.firstOrNull()

        val bestBottom = bottoms.find {
            (if (isAthletic) it.name.lowercase().contains("legging") || it.name.lowercase().contains("short") else true) &&
                    (it.formality == targetFormality || it.formality == "Semi-Formal")
        } ?: bottoms.firstOrNull()

        val bestShoes = shoes.find {
            if (isAthletic) it.name.lowercase().contains("trainer") else it.formality == targetFormality
        } ?: shoes.firstOrNull()

        val tempVal = _temperature.value.replace("°F", "").toIntOrNull() ?: 68
        val needsJacket = tempVal < 65 || _weatherCondition.value.lowercase().contains("rain") || _weatherCondition.value.lowercase().contains("snow")
        val bestJacket = if (needsJacket) {
            jackets.find {
                if (_weatherCondition.value.lowercase().contains("rain")) it.name.lowercase().contains("windbreaker") else true
            } ?: jackets.firstOrNull()
        } else {
            null
        }

        val bestAccessory = if (isFormalEvent) accessories.firstOrNull() else null

        val computedTitle = when {
            isAthletic -> "Core High-Performance"
            isFormalEvent -> "Tailored Corporate Elite"
            needsJacket -> "Cozy Urban Layer"
            else -> "Effortless Smart Casual"
        }

        val commentStr = StringBuilder()
        commentStr.append("Recommended for your ${_currentEvent.value} in ${_temperature.value} ${_weatherCondition.value} conditions. ")
        if (bestTop != null && bestBottom != null) {
            commentStr.append("Pairing the fine ${bestTop.color} ${bestTop.name} with structured ${bestBottom.color} ${bestBottom.name} delivers a high-contrast harmonious balance. ")
        }
        if (needsJacket && bestJacket != null) {
            commentStr.append("Layer with the ${bestJacket.name} for protection against breeze and damp air. ")
        }
        if (_weatherCondition.value.lowercase().contains("rain")) {
            commentStr.append("Take an umbrella to prevent styling blemishes.")
        } else {
            commentStr.append("Finish off with classic style highlights.")
        }

        _proposedOutfitTitle.value = computedTitle
        _proposedCommentary.value = commentStr.toString()

        _proposedTop.value = bestTop
        _proposedBottom.value = bestBottom
        _proposedShoes.value = bestShoes
        _proposedJacket.value = bestJacket
        _proposedAccessory.value = bestAccessory
    }

    fun runWardrobeAnalysis() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        _isAnalyzingWardrobe.value = true
        viewModelScope.launch {
            val items = clothingItems.value
            val profile = styleProfile.value

            if (hasKey) {
                try {
                    val availableItemsFormatted = JSONArray().apply {
                        items.forEach {
                            put(JSONObject().apply {
                                put("id", it.id)
                                put("name", it.name)
                                put("type", it.type)
                                put("color", it.color)
                                put("season", it.season)
                                put("formality", it.formality)
                                put("pattern", it.pattern)
                            })
                        }
                    }.toString()

                    val profileFormatted = JSONObject().apply {
                        put("preferredStyle", profile.preferredStyle)
                        put("bodyFit", profile.bodyFit)
                        put("likedColors", profile.likedColors)
                        put("avoidedColors", profile.avoidedColors)
                        put("workDressCode", profile.workDressCode)
                        put("budget", profile.budget)
                    }.toString()

                    val (commentary, missingArray) = GeminiService.getWardrobeGapAnalysis(
                        apiKey = apiKey,
                        styleProfileJson = profileFormatted,
                        currentItemsJson = availableItemsFormatted
                    )

                    val recs = mutableListOf<MissingItemRec>()
                    for (i in 0 until missingArray.size) {
                        val obj = missingArray[i]
                        val name = obj.optString("name", "Sophisticated Addition")
                        val type = obj.optString("type", "Top")
                        val whyNeeded = obj.optString("whyNeeded", "Elevates your daily coordinates.")
                        val searchKeyword = obj.optString("searchKeyword", name)
                        val priceRange = obj.optString("suggestedPriceRange", "$40 - $100")
                        val altColors = mutableListOf<String>()
                        obj.optJSONArray("alternativeColors")?.let { arr ->
                            for (j in 0 until arr.length()) altColors.add(arr.optString(j))
                        }
                        val suggestedBrands = mutableListOf<String>()
                        obj.optJSONArray("suggestedBrands")?.let { arr ->
                            for (j in 0 until arr.length()) suggestedBrands.add(arr.optString(j))
                        }

                        recs.add(
                            MissingItemRec(
                                name = name,
                                type = type,
                                whyNeeded = whyNeeded,
                                searchKeyword = searchKeyword,
                                priceRange = priceRange,
                                altColors = altColors.ifEmpty { listOf("Navy", "Black") },
                                suggestedBrands = suggestedBrands.ifEmpty { listOf("Everlane", "COS") }
                            )
                        )
                    }

                    val counts = items.groupBy { it.type }.mapValues { it.value.size }
                    val filledCats = listOf("Top", "Bottom", "Shoes", "Jacket", "Accessory").count { counts.containsKey(it) && (counts[it] ?: 0) > 0 }
                    val completeness = (35 + filledCats * 12).coerceAtLeast(40).coerceAtMost(95)

                    _wardrobeAnalysis.value = WardrobeAnalysisResult(
                        completenessScore = completeness,
                        feedback = commentary,
                        recommendations = recs,
                        countsByType = counts
                    )
                } catch (e: Exception) {
                    Log.e("FashionViewModel", "AI wardrobe analyzer failed, falling back to local engine", e)
                    runLocalWardrobeAnalysis(items, profile)
                } finally {
                    _isAnalyzingWardrobe.value = false
                }
            } else {
                kotlinx.coroutines.delay(1000)
                runLocalWardrobeAnalysis(items, profile)
                _isAnalyzingWardrobe.value = false
            }
        }
    }

    private fun runLocalWardrobeAnalysis(items: List<ClothingItem>, profile: StyleProfile) {
        val counts = items.groupBy { it.type }.mapValues { it.value.size }
        val tops = counts["Top"] ?: 0
        val bottoms = counts["Bottom"] ?: 0
        val shoes = counts["Shoes"] ?: 0
        val jackets = counts["Jacket"] ?: 0
        val accessories = counts["Accessory"] ?: 0

        val filledCats = listOf("Top", "Bottom", "Shoes", "Jacket", "Accessory").count { counts.containsKey(it) && (counts[it] ?: 0) > 0 }
        val completeness = (35 + filledCats * 12).coerceAtLeast(40).coerceAtMost(95)

        val brands = when (profile.budget.lowercase()) {
            "budget" -> listOf("Uniqlo", "H&M", "Zara")
            "premium" -> listOf("COS", "Ralph Lauren", "Hugo Boss", "Theory")
            else -> listOf("Everlane", "J.Crew", "Bonobos", "Nordstrom")
        }

        val prices = when (profile.budget.lowercase()) {
            "budget" -> listOf("$19 - $49", "$29 - $59", "$39 - $79")
            "premium" -> listOf("$150 - $350", "$200 - $450", "$120 - $280")
            else -> listOf("$48 - $98", "$68 - $128", "$88 - $148")
        }

        val missing = mutableListOf<MissingItemRec>()

        if (tops < 3) {
            missing.add(
                MissingItemRec(
                    name = "Premium Cotton Oxford Shirt",
                    type = "Top",
                    whyNeeded = "A high-quality Oxford button-down is a timeless foundational layer. It perfectly supports your '${profile.preferredStyle}' wardrobe objectives and works under knitwear or alone for professional events.",
                    searchKeyword = "${brands[0]} classic white oxford cotton shirt",
                    priceRange = prices[0],
                    altColors = listOf("Light Blue", "Off-White"),
                    suggestedBrands = listOf(brands[0], "Everlane", "Brooks Brothers")
                )
            )
        }

        if (bottoms < 3) {
            missing.add(
                MissingItemRec(
                    name = "Slim Pleated Chino Trousers",
                    type = "Bottom",
                    whyNeeded = "Chinos bridge the gap between casual denim and formal slacks. Given your preferred clothing fit is '${profile.bodyFit}', a tapered chino provides clean proportions and absolute comfort.",
                    searchKeyword = "men taper stretch beige chino pants",
                    priceRange = prices[1],
                    altColors = listOf("Tan Beige", "Midnight Navy"),
                    suggestedBrands = listOf(brands.getOrElse(1) { "Bonobos" }, "Uniqlo", "Lululemon")
                )
            )
        }

        if (shoes < 2) {
            val shoename = if (profile.workDressCode.contains("Formal")) "Leather Monkstrap Derby Shoes" else "Minimalist Full-Grain Leather Sneakers"
            val why = if (profile.workDressCode.contains("Formal")) {
                "Your work code is '${profile.workDressCode}', but your closet lacks premium formal footwear. These Derby shoes provide the refined polish required to command respect in professional settings."
            } else {
                "Every '${profile.preferredStyle}' wardrobe revolves around clean, low-profile leather sneakers. They offer effortless modern styling with smart-casual versatility."
            }
            missing.add(
                MissingItemRec(
                    name = shoename,
                    type = "Shoes",
                    whyNeeded = why,
                    searchKeyword = "premium plain white leather low sneakers men",
                    priceRange = prices[2],
                    altColors = listOf("Matte Black", "Cognac Brown"),
                    suggestedBrands = listOf(brands.getOrElse(2) { "Clarks" }, "Common Projects", "Cole Haan")
                )
            )
        }

        if (jackets < 2) {
            missing.add(
                MissingItemRec(
                    name = "Structured Transitional Trench Coat",
                    type = "Jacket",
                    whyNeeded = "Outerwear completes the silhouette. A mid-length trench provides weather protection that easily matches your preferred '${profile.likedColors}' accents without bulk.",
                    searchKeyword = "mens lightweight tailored rain trench coat",
                    priceRange = prices[0],
                    altColors = listOf("Olive Green", "Charcoal Slate"),
                    suggestedBrands = listOf(brands[0], "Patagonia", "Theory")
                )
            )
        }

        if (accessories < 2) {
            missing.add(
                MissingItemRec(
                    name = "Minimalist Leather Strap Watch",
                    type = "Accessory",
                    whyNeeded = "A classic analog wrist timepiece anchors accessories. It adds a subtle layer of personal curation and meticulous elegance to your '${profile.preferredStyle}' looks.",
                    searchKeyword = "minimalist white dial brown leather strap watch",
                    priceRange = prices[1],
                    altColors = listOf("Saddle Tan", "All Black"),
                    suggestedBrands = listOf("Seiko", "Timex", "Daniel Wellington")
                )
            )
        }

        if (missing.size < 3) {
            missing.add(
                MissingItemRec(
                    name = "Unstructured Stretch Wool Blazer",
                    type = "Jacket",
                    whyNeeded = "Adds instant elevate on top of basic tees. A relaxed unlined stretch blazer maintains the casual charm of your style while instantly stepping up formality as and when required.",
                    searchKeyword = "unstructured travel sport coat wool blend blazer",
                    priceRange = prices[2],
                    altColors = listOf("Navy", "Heather Gray"),
                    suggestedBrands = listOf("Theory", "Everlane", "Nordstrom Signature")
                )
            )
        }

        val feedbackStr = java.lang.StringBuilder()
        feedbackStr.append("At $completeness% completeness, you have established a beautiful foundation of ${items.size} pieces. ")
        feedbackStr.append("Your wardrobe is deeply tailored to your preferred '${profile.preferredStyle}' aesthetic. ")
        if (profile.likedColors.isNotEmpty()) {
            feedbackStr.append("The liked colors ('${profile.likedColors}') are beautifully represented. ")
        }
        feedbackStr.append("However, by strategically infusing these 3 specific items, you can unlock up to 25+ new outfit combinations!")

        _wardrobeAnalysis.value = WardrobeAnalysisResult(
            completenessScore = completeness,
            feedback = feedbackStr.toString(),
            recommendations = missing.take(3),
            countsByType = counts
        )
    }
}
