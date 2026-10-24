package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.Mood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

data class QueuedSentenceAudio(
    val index: Int,
    val text: String,
    val wavFile: File
)

data class TtsPlaybackStatus(
    val isProcessing: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSentenceIndex: Int = 0,
    val totalSentences: Int = 0,
    val activeSentenceText: String = "",
    val generatedWavCount: Int = 0
)

class AudioQueuePlayer(
    private val context: Context,
    private val ttsEngine: EmbeddedTtsEngine,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(TtsPlaybackStatus())
    val status: StateFlow<TtsPlaybackStatus> = _status.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var producerJob: Job? = null
    private var consumerJob: Job? = null

    // Channel passing generated audio items from synthesis producer to playback consumer
    private var audioChannel = Channel<QueuedSentenceAudio>(Channel.UNLIMITED)
    private val generatedAudios = mutableListOf<QueuedSentenceAudio>()

    fun startPipeline(
        sentences: List<String>,
        mood: Mood,
        speechRate: Float = 1.0f,
        speechPitch: Float = 1.0f
    ) {
        stopAll()

        if (sentences.isEmpty()) return

        audioChannel = Channel(Channel.UNLIMITED)
        generatedAudios.clear()

        _status.value = TtsPlaybackStatus(
            isProcessing = true,
            isPlaying = false,
            currentSentenceIndex = 0,
            totalSentences = sentences.size,
            activeSentenceText = sentences.first(),
            generatedWavCount = 0
        )

        // 1. PRODUCER COROUTINE: Synthesizes sentences in order
        producerJob = scope.launch(Dispatchers.IO) {
            for ((index, sentence) in sentences.withIndex()) {
                val wavFile = ttsEngine.synthesizeSentenceToWav(
                    sentence = sentence,
                    mood = mood,
                    sentenceIndex = index,
                    speechRate = speechRate,
                    speechPitch = speechPitch
                )

                val item = QueuedSentenceAudio(
                    index = index,
                    text = sentence,
                    wavFile = wavFile
                )
                generatedAudios.add(item)
                _status.value = _status.value.copy(generatedWavCount = generatedAudios.size)
                audioChannel.send(item)
            }
            audioChannel.close()
            _status.value = _status.value.copy(isProcessing = false)
        }

        // 2. CONSUMER COROUTINE: Plays WAV files in order as soon as they become ready
        consumerJob = scope.launch(Dispatchers.Main) {
            for (audioItem in audioChannel) {
                _status.value = _status.value.copy(
                    isPlaying = true,
                    currentSentenceIndex = audioItem.index,
                    activeSentenceText = audioItem.text
                )

                playWavAndWait(audioItem.wavFile)
            }

            _status.value = _status.value.copy(
                isPlaying = false,
                activeSentenceText = ""
            )
        }
    }

    private suspend fun playWavAndWait(wavFile: File) = withContext(Dispatchers.Main) {
        if (!wavFile.exists()) return@withContext

        val playJob = Job()
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(wavFile))
                setOnCompletionListener {
                    if (playJob.isActive) playJob.complete()
                }
                setOnErrorListener { _, _, _ ->
                    if (playJob.isActive) playJob.complete()
                    true
                }
                prepare()
                start()
            }
            playJob.join()
        } catch (e: Exception) {
            e.printStackTrace()
            if (playJob.isActive) playJob.complete()
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _status.value = _status.value.copy(isPlaying = false)
            } else {
                player.start()
                _status.value = _status.value.copy(isPlaying = true)
            }
        }
    }

    fun replayAll(mood: Mood) {
        if (generatedAudios.isEmpty()) return
        stopPlaybackOnly()

        consumerJob = scope.launch(Dispatchers.Main) {
            for (audioItem in generatedAudios) {
                _status.value = _status.value.copy(
                    isPlaying = true,
                    currentSentenceIndex = audioItem.index,
                    activeSentenceText = audioItem.text
                )
                playWavAndWait(audioItem.wavFile)
            }
            _status.value = _status.value.copy(isPlaying = false, activeSentenceText = "")
        }
    }

    fun stopAll() {
        producerJob?.cancel()
        consumerJob?.cancel()
        producerJob = null
        consumerJob = null

        stopPlaybackOnly()

        _status.value = TtsPlaybackStatus(
            isProcessing = false,
            isPlaying = false,
            currentSentenceIndex = 0,
            totalSentences = 0,
            activeSentenceText = "",
            generatedWavCount = 0
        )
    }

    private fun stopPlaybackOnly() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) { }
        mediaPlayer = null
    }

    fun release() {
        stopAll()
        // Clean up cache wav files
        scope.launch(Dispatchers.IO) {
            try {
                File(context.cacheDir, "tts_cache").deleteRecursively()
            } catch (_: Exception) {}
        }
    }
}
