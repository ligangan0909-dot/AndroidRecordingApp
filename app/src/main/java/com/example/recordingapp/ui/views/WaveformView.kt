package com.example.recordingapp.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.recordingapp.R

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val amplitudes = mutableListOf<Float>()
    private val maxAmplitudes = 60
    
    init {
        paint.color = ContextCompat.getColor(context, R.color.orange_primary)
        paint.strokeWidth = 4f
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
        val spacing = 2f
        
        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barWidth / 2
            val barHeight = amplitude * height / 2f * 0.9f
            
            val alpha = if (index > amplitudes.size - 10) {
                255
            } else {
                (100 + (index.toFloat() / amplitudes.size * 155)).toInt()
            }
            paint.alpha = alpha
            
            canvas.drawLine(
                x,
                centerY - barHeight,
                x,
                centerY + barHeight,
                paint
            )
        }
        
        paint.alpha = 255
    }
}
