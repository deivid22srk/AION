package com.aion.aicontroller.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.aion.aicontroller.data.AIAction
import com.aion.aicontroller.data.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AIAccessibilityService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())
    
    private var screenWidth = 0
    private var screenHeight = 0
    
    companion object {
        private const val TAG = "AIAccessibilityService"
        private var instance: AIAccessibilityService? = null
        
        fun getInstance(): AIAccessibilityService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        
        Log.d(TAG, "Service created. Screen: ${screenWidth}x${screenHeight}")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
        Toast.makeText(this, "AION Accessibility Service Ativado", Toast.LENGTH_SHORT).show()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Service destroyed")
    }
    
    fun takeScreenshot(callback: (Bitmap?) -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                { runnable -> handler.post(runnable) },
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                        try {
                            val bitmap = Bitmap.wrapHardwareBuffer(
                                screenshot.hardwareBuffer,
                                screenshot.colorSpace
                            )
                            callback(bitmap)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing screenshot: ${e.message}", e)
                            callback(null)
                        }
                    }
                    
                    override fun onFailure(errorCode: Int) {
                        Log.e(TAG, "Screenshot failed with error code: $errorCode")
                        callback(null)
                    }
                }
            )
        } else {
            Log.e(TAG, "Screenshot not supported on this Android version")
            callback(null)
        }
    }
    
    fun executeAction(action: AIAction, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Executing action: ${action.type}")
        
        when (action.type) {
            ActionType.CLICK -> {
                if (action.x != null && action.y != null) {
                    performClick(action.x, action.y, callback)
                } else {
                    callback(false)
                }
            }
            
            ActionType.LONG_CLICK -> {
                if (action.x != null && action.y != null) {
                    performLongClick(action.x, action.y, callback)
                } else {
                    callback(false)
                }
            }
            
            ActionType.TYPE_TEXT -> {
                if (action.text != null) {
                    performTypeText(action.text, callback)
                } else {
                    callback(false)
                }
            }
            
            ActionType.SCROLL -> {
                performScroll(action.direction ?: "DOWN", action.amount ?: 500, callback)
            }
            
            ActionType.SWIPE -> {
                performSwipe(action.direction ?: "UP", callback)
            }
            
            ActionType.BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                callback(true)
            }
            
            ActionType.HOME -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                callback(true)
            }
            
            ActionType.RECENT_APPS -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
                callback(true)
            }
            
            ActionType.OPEN_APP -> {
                if (action.target != null) {
                    openApp(action.target, callback)
                } else {
                    callback(false)
                }
            }
            
            ActionType.WAIT -> {
                handler.postDelayed({
                    callback(true)
                }, action.amount?.toLong() ?: 1000L)
            }
            
            ActionType.TAKE_SCREENSHOT -> {
                takeScreenshot { bitmap ->
                    callback(bitmap != null)
                }
            }
        }
    }
    
    private fun performClick(x: Int, y: Int, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Executando click em ($x, $y)")
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        
        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Click completed at ($x, $y)")
                callback(true)
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e(TAG, "Click cancelled")
                callback(false)
            }
        }, null)
    }
    
    private fun performLongClick(x: Int, y: Int, callback: (Boolean) -> Unit) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
        
        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Long click completed at ($x, $y)")
                callback(true)
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e(TAG, "Long click cancelled")
                callback(false)
            }
        }, null)
    }
    
    private fun performTypeText(text: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Digitando texto: $text")
        try {
            val focusedNode = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            
            if (focusedNode != null) {
                val arguments = android.os.Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                focusedNode.recycle()
                callback(success)
            } else {
                Log.e(TAG, "No focused input field found")
                callback(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error typing text: ${e.message}", e)
            callback(false)
        }
    }
    
    private fun performScroll(direction: String, amount: Int, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Executando scroll: direção=$direction, amount=$amount")
        val startX = screenWidth / 2f
        val startY = screenHeight / 2f
        
        val (endX, endY) = when (direction.uppercase()) {
            "DOWN" -> startX to (startY - amount)
            "UP" -> startX to (startY + amount)
            "LEFT" -> (startX + amount) to startY
            "RIGHT" -> (startX - amount) to startY
            else -> startX to (startY - amount)
        }
        
        performSwipeGesture(startX, startY, endX, endY, 300, callback)
    }
    
    private fun performSwipe(direction: String, callback: (Boolean) -> Unit) {
        Log.d(TAG, "Executando swipe: direção=$direction")
        val startX = screenWidth / 2f
        val startY = screenHeight / 2f
        
        val (endX, endY) = when (direction.uppercase()) {
            "UP" -> startX to (screenHeight * 0.2f)
            "DOWN" -> startX to (screenHeight * 0.8f)
            "LEFT" -> (screenWidth * 0.2f) to startY
            "RIGHT" -> (screenWidth * 0.8f) to startY
            else -> startX to (screenHeight * 0.2f)
        }
        
        performSwipeGesture(startX, startY, endX, endY, 400, callback)
    }
    
    private fun performSwipeGesture(
        startX: Float, 
        startY: Float, 
        endX: Float, 
        endY: Float, 
        duration: Long,
        callback: (Boolean) -> Unit
    ) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        
        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Swipe completed")
                callback(true)
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e(TAG, "Swipe cancelled")
                callback(false)
            }
        }, null)
    }
    
    private fun openApp(appName: String, callback: (Boolean) -> Unit) {
        try {
            Log.d(TAG, "Tentando abrir app: $appName")
            val packageName = getPackageNameForApp(appName)
            Log.d(TAG, "Package name: $packageName")
            
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(launchIntent)
                Log.d(TAG, "App $appName aberto com sucesso")
                callback(true)
            } else {
                Log.e(TAG, "N\u00e3o foi poss\u00edvel encontrar o app: $appName (package: $packageName)")
                callback(false)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir app: ${e.message}", e)
            callback(false)
        }
    }
    
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true ||
            node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val result = findNodeByText(child, text)
                if (result != null) {
                    return result
                }
                child.recycle()
            }
        }
        
        return null
    }
    
    private fun getPackageNameForApp(appName: String): String {
        val commonApps = mapOf(
            "chrome" to "com.android.chrome",
            "youtube" to "com.google.android.youtube",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "twitter" to "com.twitter.android",
            "telegram" to "org.telegram.messenger",
            "camera" to "com.android.camera",
            "gallery" to "com.google.android.apps.photos",
            "settings" to "com.android.settings",
            "clock" to "com.google.android.deskclock",
            "calculator" to "com.google.android.calculator"
        )
        
        return commonApps[appName.lowercase()] ?: appName
    }
}
