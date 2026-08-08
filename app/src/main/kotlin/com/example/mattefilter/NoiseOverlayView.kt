package com.example.mattefilter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * Draws a soft white haze plus a tiled speckle/grain texture on top of whatever
 * is behind it, approximating the light-diffusing look of a matte (frosted)
 * screen protector. This does not blur live content -- it layers a static
 * texture, which is cheap, battery-friendly, and doesn't require screen
 * capture permissions.
 */
class NoiseOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var intensity: Int = 40 // 0-100
    private val hazePaint = Paint()
    private val noisePaint = Paint()
    private var noiseShader: BitmapShader? = null
    private val tileSize = 128

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        buildNoiseTexture()
        applyIntensity()
    }

    fun setIntensity(value: Int) {
        intensity = value.coerceIn(0, 100)
        applyIntensity()
        invalidate()
    }

    private fun applyIntensity() {
        // Soft white wash -- scales with intensity, capped so it never fully whites out.
        val hazeAlpha = (intensity * 0.9f).toInt().coerceIn(0, 90)
        hazePaint.color = Color.argb(hazeAlpha, 255, 255, 255)

        // Grain opacity -- scales a bit faster than the haze so texture stays visible.
        val noiseAlpha = (intensity * 1.4f).toInt().coerceIn(0, 140)
        noisePaint.alpha = noiseAlpha
        noisePaint.shader = noiseShader
    }

    private fun buildNoiseTexture() {
        val bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        val rand = Random(System.nanoTime())
        for (x in 0 until tileSize) {
            for (y in 0 until tileSize) {
                val speck = rand.nextFloat()
                val pixel = when {
                    speck > 0.985f -> Color.argb(200, 255, 255, 255)
                    speck > 0.96f -> Color.argb(90, 255, 255, 255)
                    speck < 0.02f -> Color.argb(40, 0, 0, 0)
                    else -> Color.TRANSPARENT
                }
                bitmap.setPixel(x, y, pixel)
            }
        }
        noiseShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(hazePaint.color)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), noisePaint)
    }
}
