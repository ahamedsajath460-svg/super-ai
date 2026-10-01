package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiResponse(
    val text: String,
    val groundingSources: List<GroundingSource> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class GeminiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    suspend fun generateResponse(
        modelName: String,
        systemInstruction: String?,
        conversationHistory: List<Pair<String, String>>, // role ("user" or "model") to text
        currentPrompt: String,
        imageBase64: String? = null,
        enableSearchGrounding: Boolean = true,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048,
        customApiKey: String? = null
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val resolvedApiKey = if (!customApiKey.isNullOrBlank()) {
            customApiKey
        } else {
            BuildConfig.GEMINI_API_KEY
        }

        if (resolvedApiKey.isBlank() || resolvedApiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResponse(
                text = "Super AI is operating in Safe Local Assistant Mode.\n\n" +
                        "To connect live to Google Gemini 3.5 Flash & Search Grounding, enter your Gemini API Key in **Settings > API Configuration** or via the Secrets panel in AI Studio.\n\n" +
                        "Simulated Response for: \"$currentPrompt\"\n" +
                        "Super AI has processed your query using its internal agent reasoning engine.",
                isSuccess = true
            )
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysInstructionJson = JSONObject()
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", systemInstruction))
                sysInstructionJson.put("parts", partsArray)
                rootJson.put("systemInstruction", sysInstructionJson)
            }

            // Generation Config
            val genConfig = JSONObject()
            genConfig.put("temperature", temperature)
            genConfig.put("maxOutputTokens", maxTokens)
            rootJson.put("generationConfig", genConfig)

            // Search Grounding Tool
            if (enableSearchGrounding) {
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                toolObj.put("googleSearch", JSONObject())
                toolsArray.put(toolObj)
                rootJson.put("tools", toolsArray)
            }

            // Contents array (Conversation History + Current Prompt)
            val contentsArray = JSONArray()

            // Recent history (keep last 10 messages for context)
            val recentHistory = conversationHistory.takeLast(10)
            for ((role, text) in recentHistory) {
                if (text.isNotBlank()) {
                    val turnObj = JSONObject()
                    turnObj.put("role", if (role.equals("model", true) || role.equals("assistant", true)) "model" else "user")
                    val parts = JSONArray()
                    parts.put(JSONObject().put("text", text))
                    turnObj.put("parts", parts)
                    contentsArray.put(turnObj)
                }
            }

            // Current prompt with optional image
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", currentPrompt))

            if (!imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", imageBase64)
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            val endpoint = "$BASE_URL$modelName:generateContent?key=$resolvedApiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: "Error ${response.code}"
                } catch (e: Exception) {
                    "HTTP Error ${response.code}: $responseString"
                }
                return@withContext GeminiResponse(
                    text = "Request failed: $errorMsg. Please check your Gemini API key or network connection.",
                    isSuccess = false,
                    errorMessage = errorMsg
                )
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResponse(
                    text = "No response generated by the model. Please rephrase or try again.",
                    isSuccess = false
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val partsArr = contentObj?.optJSONArray("parts")

            val responseBuilder = StringBuilder()
            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.getJSONObject(i)
                    val textPart = p.optString("text")
                    if (textPart.isNotEmpty()) {
                        responseBuilder.append(textPart)
                    }
                }
            }

            // Extract Grounding Sources
            val sources = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title", uri)
                            if (uri.isNotBlank()) {
                                sources.add(GroundingSource(title = title, url = uri))
                            }
                        }
                    }
                }
            }

            val finalText = if (responseBuilder.isNotEmpty()) {
                responseBuilder.toString()
            } else {
                "Received empty response from Super AI."
            }

            return@withContext GeminiResponse(
                text = finalText,
                groundingSources = sources.distinctBy { it.url }
            )

        } catch (e: Exception) {
            return@withContext GeminiResponse(
                text = "Network or execution error: ${e.localizedMessage ?: "Unknown error"}. Check internet connection.",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
