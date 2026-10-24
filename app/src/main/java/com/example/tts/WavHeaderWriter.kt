package com.example.tts

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavHeaderWriter {

    fun createWavFile(
        outputFile: File,
        pcmData: ByteArray,
        sampleRate: Int = 22050,
        channels: Short = 1,
        bitsPerSample: Short = 16
    ) {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = (channels * (bitsPerSample / 8)).toShort()

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)

            // RIFF chunk
            put('R'.code.toByte())
            put('I'.code.toByte())
            put('F'.code.toByte())
            put('F'.code.toByte())
            putInt(totalDataLen)
            put('W'.code.toByte())
            put('A'.code.toByte())
            put('V'.code.toByte())
            put('E'.code.toByte())

            // "fmt " chunk
            put('f'.code.toByte())
            put('m'.code.toByte())
            put('t'.code.toByte())
            put(' '.code.toByte())
            putInt(16) // SubChunk1Size for PCM
            putShort(1) // AudioFormat 1 = PCM
            putShort(channels)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign)
            putShort(bitsPerSample)

            // "data" chunk
            put('d'.code.toByte())
            put('a'.code.toByte())
            put('t'.code.toByte())
            put('a'.code.toByte())
            putInt(totalAudioLen)
        }.array()

        FileOutputStream(outputFile).use { fos ->
            fos.write(header)
            fos.write(pcmData)
            fos.flush()
        }
    }
}
