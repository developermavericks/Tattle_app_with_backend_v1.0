package com.example.tattle.utils

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

object BlurHashDecoder {

    fun decode(blurHash: String?, width: Int = 32, height: Int = 18, punch: Float = 1f): ImageBitmap? {
        if (blurHash.isNullOrBlank() || blurHash.length < 6) return null

        val numCompEnc = decode83(blurHash, 0, 1) ?: return null
        val numCompX = (numCompEnc % 9) + 1
        val numCompY = (numCompEnc / 9) + 1

        val maxAcEnc = decode83(blurHash, 1, 2) ?: return null
        val maxAc = (maxAcEnc + 1) / 166f

        val expectedLength = 1 + 1 + 4 + (numCompX * numCompY - 1) * 2
        if (blurHash.length < expectedLength) return null

        val colors = Array(numCompX * numCompY) { FloatArray(3) }

        val dcEnc = decode83(blurHash, 2, 6) ?: return null
        colors[0] = decodeDc(dcEnc)

        var accIndex = 6
        for (i in 1 until numCompX * numCompY) {
            val acEnc = decode83(blurHash, accIndex, accIndex + 2) ?: return null
            colors[i] = decodeAc(acEnc, maxAc * punch)
            accIndex += 2
        }

        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f

                for (j in 0 until numCompY) {
                    for (i in 0 until numCompX) {
                        val basis = (cos(PI * x * i / width) * cos(PI * y * j / height)).toFloat()
                        val color = colors[j * numCompX + i]
                        r += color[0] * basis
                        g += color[1] * basis
                        b += color[2] * basis
                    }
                }

                val red = linearToSrgb(r)
                val green = linearToSrgb(g)
                val blue = linearToSrgb(b)

                paint.color = Color(red, green, blue)
                canvas.drawRect(
                    left = x.toFloat(),
                    top = y.toFloat(),
                    right = (x + 1).toFloat(),
                    bottom = (y + 1).toFloat(),
                    paint = paint
                )
            }
        }

        return bitmap
    }

    private fun decode83(str: String, start: Int, end: Int): Int? {
        var value = 0
        for (i in start until end) {
            val c = str.getOrNull(i) ?: return null
            val index = char83Map[c] ?: return null
            value = value * 83 + index
        }
        return value
    }

    private fun decodeDc(value: Int): FloatArray {
        val r = value shr 16
        val g = (value shr 8) and 255
        val b = value and 255
        return floatArrayOf(srgbToLinear(r), srgbToLinear(g), srgbToLinear(b))
    }

    private fun decodeAc(value: Int, maxAc: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signPow((r - 9) / 9f, 2f) * maxAc,
            signPow((g - 9) / 9f, 2f) * maxAc,
            signPow((b - 9) / 9f, 2f) * maxAc
        )
    }

    private fun srgbToLinear(value: Int): Float {
        val v = value / 255f
        return if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
    }

    private fun linearToSrgb(value: Float): Int {
        val v = value.coerceIn(0f, 1f)
        val srgb = if (v <= 0.0031308f) v * 12.92f else 1.055f * v.pow(1f / 2.4f) - 0.055f
        return (srgb * 255f + 0.5f).toInt().coerceIn(0, 255)
    }

    private fun signPow(valIn: Float, exp: Float): Float {
        return valIn.pow(exp)
    }

    private val char83Map = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~"
        .mapIndexed { index, c -> c to index }
        .toMap()
}
