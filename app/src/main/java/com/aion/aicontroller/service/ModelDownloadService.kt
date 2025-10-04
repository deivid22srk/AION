package com.aion.aicontroller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aion.aicontroller.MainActivity
import com.aion.aicontroller.data.LocalVisionModel
import com.aion.aicontroller.local.LocalModelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DownloadState(
    val modelId: String? = null,
    val stage: String = "",
    val progress: Int = 0,
    val isDownloading: Boolean = false,
    val error: String? = null
)

class ModelDownloadService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var notificationManager: NotificationManager? = null
    private var currentDownloadJob: Job? = null
    
    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState
    
    companion object {
        private const val TAG = "ModelDownloadService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "model_download_channel"
        private const val CHANNEL_NAME = "Download de Modelos"
        
        fun start(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): ModelDownloadService = this@ModelDownloadService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Aguardando download...", 0))
        return START_STICKY
    }
    
    fun downloadModel(model: LocalVisionModel) {
        if (_downloadState.value.isDownloading) {
            Log.w(TAG, "Download já em andamento")
            return
        }
        
        Log.d(TAG, "Iniciando download: ${model.name}")
        
        currentDownloadJob?.cancel()
        currentDownloadJob = serviceScope.launch {
            try {
                _downloadState.value = DownloadState(
                    modelId = model.id,
                    stage = "Preparando...",
                    progress = 0,
                    isDownloading = true
                )
                
                updateNotification("Baixando ${model.name}", 0)
                
                val modelManager = LocalModelManager(this@ModelDownloadService)
                
                // Download do modelo principal
                _downloadState.value = _downloadState.value.copy(
                    stage = "Baixando modelo (1/2)..."
                )
                
                modelManager.downloadModel(model).collect { progress ->
                    val overallProgress = progress.percentage / 2
                    
                    _downloadState.value = _downloadState.value.copy(
                        progress = overallProgress
                    )
                    
                    updateNotification(
                        "Baixando ${model.name} (1/2)",
                        overallProgress
                    )
                }
                
                // Download do mmproj
                _downloadState.value = _downloadState.value.copy(
                    stage = "Baixando mmproj (2/2)..."
                )
                
                modelManager.downloadMMProj(model).collect { progress ->
                    val overallProgress = 50 + (progress.percentage / 2)
                    
                    _downloadState.value = _downloadState.value.copy(
                        progress = overallProgress
                    )
                    
                    updateNotification(
                        "Baixando ${model.name} (2/2)",
                        overallProgress
                    )
                }
                
                // Concluído
                _downloadState.value = DownloadState(
                    modelId = model.id,
                    stage = "Concluído!",
                    progress = 100,
                    isDownloading = false
                )
                
                showCompletionNotification("${model.name} baixado com sucesso!")
                
                Log.d(TAG, "Download concluído: ${model.name}")
                
                // Limpar estado após 3 segundos
                kotlinx.coroutines.delay(3000)
                _downloadState.value = DownloadState()
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro no download: ${e.message}", e)
                
                _downloadState.value = DownloadState(
                    modelId = model.id,
                    stage = "Erro",
                    progress = 0,
                    isDownloading = false,
                    error = e.message
                )
                
                showErrorNotification("Erro ao baixar: ${e.message}")
                
                // Limpar estado de erro após 5 segundos
                kotlinx.coroutines.delay(5000)
                _downloadState.value = DownloadState()
            }
        }
    }
    
    fun cancelDownload() {
        Log.d(TAG, "Cancelando download")
        currentDownloadJob?.cancel()
        _downloadState.value = DownloadState()
        updateNotification("Download cancelado", 0)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações de download de modelos de IA"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(text: String, progress: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AION - Download de Modelo")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        
        if (progress > 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(100, 0, true)
        }
        
        return builder.build()
    }
    
    private fun updateNotification(text: String, progress: Int) {
        val notification = createNotification(text, progress)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showCompletionNotification(text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AION - Download Concluído")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager?.notify(NOTIFICATION_ID + 1, notification)
        
        // Voltar para notificação de aguardando
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(100)
            updateNotification("Aguardando download...", 0)
        }
    }
    
    private fun showErrorNotification(text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AION - Erro no Download")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager?.notify(NOTIFICATION_ID + 2, notification)
        
        // Voltar para notificação de aguardando
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(100)
            updateNotification("Aguardando download...", 0)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        currentDownloadJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}
