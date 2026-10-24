package com.example.data.service

import com.example.data.model.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedPartnerResponse(
    val mood: Mood,
    val cleanText: String,
    val sentences: List<String>,
    val rawText: String
)

class GeminiStudyService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val DEFAULT_MODEL = "gemini-3.1-flash-lite-preview"
        const val BACKUP_MODEL = "gemini-3.5-flash"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun sendStudyQuery(
        apiKey: String,
        userQuery: String,
        personaPrompt: String,
        studyContext: String,
        modelName: String = DEFAULT_MODEL
    ): Result<ParsedPartnerResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Gemini API Key is missing. Please provide a key in the Configuration panel.")
            )
        }

        val effectiveModel = if (modelName.isNotBlank()) modelName else DEFAULT_MODEL
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=$apiKey"

        val systemInstructionText = buildSystemInstruction(personaPrompt, studyContext)

        val requestJson = JSONObject().apply {
            // User Content
            val contentsArray = JSONArray().apply {
                val userContentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", userQuery) })
                    }
                    put("parts", partsArray)
                }
                put(userContentObj)
            }
            put("contents", contentsArray)

            // System Instruction
            val sysInstructionObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                }
                put("parts", partsArray)
            }
            put("systemInstruction", sysInstructionObj)

            // Generation Config
            val genConfig = JSONObject().apply {
                put("temperature", 0.75)
                put("topP", 0.95)
                put("topK", 40)
            }
            put("generationConfig", genConfig)
        }

        val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyStr).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                    } catch (_: Exception) {
                        "HTTP ${response.code}: ${response.message}"
                    }
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext Result.failure(Exception("No candidate answers returned by Gemini."))
                }

                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val rawAnswerText = parts?.optJSONObject(0)?.optString("text")?.trim().orEmpty()

                if (rawAnswerText.isBlank()) {
                    return@withContext Result.failure(Exception("Received empty response from study partner."))
                }

                val parsed = parseResponseText(rawAnswerText)
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseResponseText(raw: String): ParsedPartnerResponse {
        val lines = raw.lines()
        val firstLine = lines.firstOrNull()?.trim().orEmpty()

        // Extract [MOOD: <MOOD>] using Regex
        val moodRegex = Regex("""\[MOOD:\s*([A-Za-z]+)\]""", RegexOption.IGNORE_CASE)
        val moodMatch = moodRegex.find(firstLine)

        val detectedMood = if (moodMatch != null) {
            val moodName = moodMatch.groupValues[1]
            Mood.fromTagOrName(moodName)
        } else {
            // Fallback: search anywhere in the first 2 lines for mood keyword
            val fallbackMatch = moodRegex.find(raw)
            if (fallbackMatch != null) {
                Mood.fromTagOrName(fallbackMatch.groupValues[1])
            } else {
                Mood.NEUTRAL
            }
        }

        // Remove the mood tag from the main display text
        val cleanText = raw
            .replace(moodRegex, "")
            .trimStart('\n', '\r', ' ')
            .trim()

        // Split the clean text strictly by sentence delimiters (., !, ?) using Regex into an Array/Queue
        // Regex lookbehind (?<=[.!?]) ensures the delimiter stays with the sentence
        val rawSentenceList = cleanText.split(Regex("""(?<=[.!?])\s+"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val finalSentences = if (rawSentenceList.isEmpty() && cleanText.isNotBlank()) {
            listOf(cleanText)
        } else {
            rawSentenceList
        }

        return ParsedPartnerResponse(
            mood = detectedMood,
            cleanText = cleanText,
            sentences = finalSentences,
            rawText = raw
        )
    }

    private fun buildSystemInstruction(personaPrompt: String, studyContext: String): String {
        return buildString {
            appendLine("=== PARTNER PERSONA & CHARACTER TRAITS ===")
            appendLine(personaPrompt.trim())
            appendLine()

            if (studyContext.isNotBlank()) {
                appendLine("=== ATTACHED STUDY MATERIAL / DOCUMENT CONTEXT ===")
                appendLine(studyContext.trim())
                appendLine("Ground your explanations and answers in this study material whenever relevant.")
                appendLine()
            }

            appendLine("=== STRICT FORMATTING RULES (MANDATORY) ===")
            appendLine("1. Response must be a single full text block.")
            appendLine("2. The very first line of your response MUST strictly start with exactly one mood tag from: [MOOD: HAPPY], [MOOD: THINKING], [MOOD: NEUTRAL], [MOOD: SURPRISED], or [MOOD: SAD].")
            appendLine("3. Do not place any other words or punctuation on that first line beside the mood tag.")
            appendLine("4. Every sentence in the subsequent body MUST strictly end with standard punctuation (., !, ?). No trailing ellipses or unpunctuated sentence fragments.")
            appendLine("5. Maintain your designated personality, tone, and character charm at all times.")
        }
    }
}
