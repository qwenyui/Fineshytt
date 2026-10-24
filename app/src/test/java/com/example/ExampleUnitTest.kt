package com.example

import com.example.data.model.Mood
import com.example.data.service.GeminiStudyService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val service = GeminiStudyService()

    @Test
    fun testParseHappyMoodAndSentences() {
        val rawResponse = """
            [MOOD: HAPPY]
            Great job on reviewing cellular respiration! Mitochondria really are the powerhouses of the cell. Are you ready for a quick quiz?
        """.trimIndent()

        val parsed = service.parseResponseText(rawResponse)

        assertEquals(Mood.HAPPY, parsed.mood)
        assertEquals(3, parsed.sentences.size)
        assertEquals("Great job on reviewing cellular respiration!", parsed.sentences[0])
        assertEquals("Mitochondria really are the powerhouses of the cell.", parsed.sentences[1])
        assertEquals("Are you ready for a quick quiz?", parsed.sentences[2])
    }

    @Test
    fun testParseThinkingMood() {
        val rawResponse = """
            [MOOD: THINKING]
            Let's dissect this calculus problem step by step. First, what is the derivative of x squared?
        """.trimIndent()

        val parsed = service.parseResponseText(rawResponse)

        assertEquals(Mood.THINKING, parsed.mood)
        assertEquals(2, parsed.sentences.size)
        assertTrue(parsed.cleanText.startsWith("Let's dissect"))
    }

    @Test
    fun testParseSurprisedMood() {
        val rawResponse = """
            [MOOD: SURPRISED]
            Whoa! You solved that entire equation in under two minutes! That's unbelievable speed.
        """.trimIndent()

        val parsed = service.parseResponseText(rawResponse)

        assertEquals(Mood.SURPRISED, parsed.mood)
        assertEquals(3, parsed.sentences.size)
    }

    @Test
    fun testParseSadMood() {
        val rawResponse = """
            [MOOD: SAD]
            Oh no, don't worry about missing that question. We will review it together until it makes sense.
        """.trimIndent()

        val parsed = service.parseResponseText(rawResponse)

        assertEquals(Mood.SAD, parsed.mood)
        assertEquals(2, parsed.sentences.size)
    }

    @Test
    fun testFallbackWhenNoMoodTag() {
        val rawResponse = "Here is the summary of Newton's laws of motion. Every action has an equal and opposite reaction."
        val parsed = service.parseResponseText(rawResponse)

        assertEquals(Mood.NEUTRAL, parsed.mood)
        assertEquals(2, parsed.sentences.size)
    }
}
