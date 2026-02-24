package com.example.recordingapp.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.recordingapp.R
import kotlin.math.abs
import kotlin.random.Random

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val amplitudes = mutableListOf<Float>()
    private val maxAmplitudes = 50
    
    init {
        paint.color = ContextCompat.getColor(context, R.color.orange_primary)
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
    }
    
    fun addAmplitude(amplitude: Float) {
        amplitudes.add(amplitude)
        if (amplitudes.size > maxAmplitudes) {
            amplitudes.removeAt(0)
        }
        invalidate()
    }
    
    fun clear() {
        amplitudes.clear()
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (amplitudes.isEmpty()) return
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f
        val barWidth = width / maxAmplitudes
        
        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2
            val barHeight = amplitude * height / 2f * 0.8f
            
            canvas.drawLine(
                x,
                centerY - barHeight,
                x,
                centerY + barHeight,
                paint
            )
        }
    }
}
