package com.example.recordingapp.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.recordingapp.R

class CircularRecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var isRecording = false
    private var rippleRadius = 0f
    private var rippleAlpha = 0
    private var rippleAnimator: ValueAnimator? = null
    
    private val buttonRadius: Float
        get() = minOf(width, height) / 2f * 0.7f
    
    init {
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.orange_primary)
        
        ripplePaint.style = Paint.Style.STROKE
        ripplePaint.strokeWidth = 4f
        ripplePaint.color = ContextCompat.getColor(context, R.color.orange_primary)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        if (isRecording) {
            ripplePaint.alpha = rippleAlpha
            canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint)
        }
        
        canvas.drawCircle(centerX, centerY, buttonRadius, paint)
        
        if (isRecording) {
            paint.color = ContextCompat.getColor(context, android.R.color.white)
            val squareSize = buttonRadius * 0.5f
            canvas.drawRect(
                centerX - squareSize / 2,
                centerY - squareSize / 2,
                centerX + squareSize / 2,
                centerY + squareSize / 2,
                paint
            )
            paint.color = ContextCompat.getColor(context, R.color.orange_primary)
        }
    }
    
    fun setRecording(recording: Boolean) {
        if (isRecording == recording) return
        
        isRecording = recording
        
        if (recording) {
            startRippleAnimation()
        } else {
            stopRippleAnimation()
        }
        
        invalidate()
    }
    
    private fun startRippleAnimation() {
        rippleAnimator?.cancel()
        
        rippleAnimator = ValueAnimator.ofFloat(buttonRadius, buttonRadius * 1.5f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { animator ->
                rippleRadius = animator.animatedValue as Float
                val fraction = animator.animatedFraction
                rippleAlpha = ((1f - fraction) * 100).toInt()
                invalidate()
            }
            
            start()
        }
    }
    
    private fun stopRippleAnimation() {
        rippleAnimator?.cancel()
        rippleAnimator = null
        invalidate()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRippleAnimation()
    }
}
