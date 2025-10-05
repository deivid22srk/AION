package com.aion.aicontroller.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat

class FloatingLogOverlay(private val context: Context) {
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var logTextView: TextView? = null
    private var scrollView: ScrollView? = null
    private var isShowing = false
    
    private val logs = mutableListOf<String>()
    
    @SuppressLint("InflateParams")
    fun show() {
        if (isShowing) return
        
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        overlayView = LayoutInflater.from(context).inflate(
            android.R.layout.simple_list_item_1, 
            null
        ).apply {
            setBackgroundColor(0xCC000000.toInt())
            setPadding(16, 16, 16, 16)
        }
        
        logTextView = overlayView?.findViewById(android.R.id.text1)
        logTextView?.apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 10f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            300,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_SECURE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        
        windowManager?.addView(overlayView, params)
        isShowing = true
        
        updateLogDisplay()
    }
    
    fun hide() {
        if (!isShowing) return
        
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        overlayView = null
        logTextView = null
        scrollView = null
        windowManager = null
        isShowing = false
    }
    
    fun addLog(message: String) {
        logs.add(message)
        if (logs.size > 50) {
            logs.removeAt(0)
        }
        updateLogDisplay()
    }
    
    fun clearLogs() {
        logs.clear()
        updateLogDisplay()
    }
    
    private fun updateLogDisplay() {
        if (!isShowing) return
        
        logTextView?.post {
            if (logs.isEmpty()) {
                logTextView?.text = "Aguardando logs..."
            } else {
                logTextView?.text = logs.takeLast(20).joinToString("\n")
            }
        }
    }
    
    fun isVisible(): Boolean = isShowing
}
