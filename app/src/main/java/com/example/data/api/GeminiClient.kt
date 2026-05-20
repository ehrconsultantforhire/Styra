package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.models.ClothingItem
import com.example.data.models.StyleProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if a valid API key is present in BuildConfig.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("placeholder", ignoreCase = true)
    }

    /**
     * Send style query to Gemini to get recommended outfit combination.
     */
    suspend fun getOutfitRecommendation(
        items: List<ClothingItem>,
        profile: StyleProfile,
        weatherTemp: Int,
        weatherDesc: String,
        eventName: String
    ): OutfitRecommendationResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext OutfitRecommendationResult.Error("API key not configured in Secrets panel.")
        }

        val parsedItemsStr = items.joinToString("\n") { item ->
            "- ID: ${item.id}, Name: ${item.name}, Type: ${item.type}, Color: ${item.color}, Season: ${item.season}, Formality: ${item.formality}, Pattern: ${item.pattern}, Brand: ${item.brand}"
        }

        val prompt = """
            You are Styra, a highly sophisticated fashion stylist. 
            Recommend a single, highly cohesive, and complete matching outfit from the user's available clothing items list below.
            
            Current Venue/Event: $eventName
            Current Weather: $weatherTemp°F, $weatherDesc
            User Style & Modesty Preferences:
            - Preferred Style Style: ${profile.preferredStyle}
            - Preferred Fit: ${profile.bodyFit}
            - Liked Colors: ${profile.likedColors}
            - Avoided Colors: ${profile.avoidedColors}
            - Work Dress Code: ${profile.workDressCode} 
            - Fashion Goals: ${profile.fashionGoals}
            - Modesty Preference: ${profile.modesty}
            
            Available Clothing Items Pool in Closet:
            $parsedItemsStr
            
            Choose:
            - One Top (or jacket/base layer combined)
            - One Bottom
            - One Pair of Shoes
            - One Jacket (only if cold, e.g. < 65°F or raining/windy)
            - One optional accessory
            
            Ensure the outfit respects their styling rules and weather protection!
            
            You MUST return a JSON object with this exact structure (no markdown formatting code blocks, just the raw JSON text):
            {
               "title": "Modern Corporate Chic / Autumn Minimalist / Casual Weekend Edge etc",
               "commentary": "Explain why this combination works for this specific event and weather context, highlighting color harmony and styling cues.",
               "topId": 12, (integer ID of recommended Top garment, or null)
               "bottomId": 34, (integer ID of recommended Bottom, or null)
               "shoesId": 56, (integer ID of recommended Shoes, or null)
               "jacketId": 78, (integer ID of recommended Jacket, or null if not recommended)
               "accessoryId": 90 (integer ID of recommended accessory, or null if not recommended)
            }
            Do not include any ```json wrapper. Simply return the JSON payload.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                // Enforce JSON format output or just prompt guidance
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.6)
                    // Requesting JSON format if possible
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful || bodyString == null) {
                    val errMsg = "HTTP error code: ${response.code}, body: ${bodyString ?: "null"}"
                    Log.e(TAG, errMsg)
                    return@withContext OutfitRecommendationResult.Error(errMsg)
                }

                try {
                    val rootJson = JSONObject(bodyString)
                    val candidates = rootJson.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    var responseText = parts.getJSONObject(0).getString("text").trim()

                    // Sanitize potential markdown wrap
                    if (responseText.startsWith("```json")) {
                        responseText = responseText.substringAfter("```json")
                    }
                    if (responseText.endsWith("```")) {
                        responseText = responseText.substringBeforeLast("```")
                    }
                    responseText = responseText.trim()

                    val resultJson = JSONObject(responseText)
                    val title = resultJson.optString("title", "Spec recommendation")
                    val commentary = resultJson.optString("commentary", "Matched custom styling preferences.")
                    val topId = if (resultJson.isNull("topId")) null else resultJson.optInt("topId")
                    val bottomId = if (resultJson.isNull("bottomId")) null else resultJson.optInt("bottomId")
                    val shoesId = if (resultJson.isNull("shoesId")) null else resultJson.optInt("shoesId")
                    val jacketId = if (resultJson.isNull("jacketId")) null else resultJson.optInt("jacketId")
                    val accessoryId = if (resultJson.isNull("accessoryId")) null else resultJson.optInt("accessoryId")

                    return@withContext OutfitRecommendationResult.Success(
                        title = title,
                        commentary = commentary,
                        topId = topId,
                        bottomId = bottomId,
                        shoesId = shoesId,
                        jacketId = jacketId,
                        accessoryId = accessoryId
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Parsing failed for response: $bodyString", e)
                    return@withContext OutfitRecommendationResult.Error("Failed to parse Gemini model response: ${e.localizedMessage}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed", e)
            return@withContext OutfitRecommendationResult.Error("Network error: ${e.localizedMessage}")
        }
    }
}

sealed class OutfitRecommendationResult {
    data class Success(
        val title: String,
        val commentary: String,
        val topId: Int?,
        val bottomId: Int?,
        val shoesId: Int?,
        val jacketId: Int?,
        val accessoryId: Int?
    ) : OutfitRecommendationResult()

    data class Error(val message: String) : OutfitRecommendationResult()
}
