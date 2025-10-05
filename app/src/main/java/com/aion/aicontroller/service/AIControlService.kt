package com.aion.aicontroller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aion.aicontroller.ai.LiteRTMultimodalController
import com.aion.aicontroller.data.Status
import com.aion.aicontroller.data.TaskStatus
import com.aion.aicontroller.data.AIAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AIControlService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    private var liteRTController: LiteRTMultimodalController? = null
    private var isRunning = false
    private var floatingLogOverlay: FloatingLogOverlay? = null
    
    private val _taskStatus = MutableStateFlow(TaskStatus(Status.IDLE))
    val taskStatus: StateFlow<TaskStatus> = _taskStatus
    
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs
    
    private val conversationHistory = mutableListOf<String>()
    
    companion object {
        private const val TAG = "AIControlService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "aion_service_channel"
        private const val MAX_STEPS = 20
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): AIControlService = this@AIControlService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        floatingLogOverlay = FloatingLogOverlay(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("AION está rodando"))
        return START_STICKY
    }
    
    fun setupLocalAI(modelPath: String, mmProjPath: String, isLiteRT: Boolean = true) {
        serviceScope.launch {
            liteRTController = LiteRTMultimodalController(this@AIControlService, modelPath)
            val loaded = liteRTController?.initialize() ?: false
            
            if (loaded) {
                addLog("✅ Modelo MediaPipe LLM Inference carregado com sucesso (GPU acceleration via LiteRT)")
            } else {
                addLog("❌ Erro ao carregar modelo MediaPipe LLM Inference")
                liteRTController = null
            }
        }
    }
    
    fun unloadModel() {
        liteRTController?.unload()
        liteRTController = null
        addLog("Modelo descarregado")
    }
    
    fun isModelLoaded(): Boolean {
        return liteRTController?.isReady() ?: false
    }
    
    fun executeTask(task: String) {
        if (isRunning) {
            addLog("Já existe uma tarefa em execução")
            return
        }
        
        if (liteRTController == null) {
            addLog("IA não configurada. Configure nas opções.")
            _taskStatus.value = TaskStatus(Status.ERROR, "IA não configurada")
            return
        }
        
        val accessibilityService = AIAccessibilityService.getInstance()
        if (accessibilityService == null) {
            addLog("Serviço de acessibilidade não está ativo")
            _taskStatus.value = TaskStatus(Status.ERROR, "Serviço de acessibilidade desativado")
            return
        }
        
        isRunning = true
        conversationHistory.clear()
        addLog("Iniciando tarefa: $task")
        _taskStatus.value = TaskStatus(Status.PROCESSING, "Iniciando tarefa...")
        
        serviceScope.launch {
            var stepCount = 0
            var taskCompleted = false
            
            while (stepCount < MAX_STEPS && !taskCompleted && isRunning) {
                stepCount++
                addLog("Passo $stepCount: Capturando tela...")
                _taskStatus.value = TaskStatus(Status.PROCESSING, "Analisando tela...", (stepCount * 100) / MAX_STEPS)
                
                var screenshot: Bitmap? = null
                accessibilityService.takeScreenshot { bitmap ->
                    screenshot = bitmap
                }
                
                delay(1000)
                
                if (screenshot == null) {
                    addLog("Erro: Não foi possível capturar a tela")
                    _taskStatus.value = TaskStatus(Status.ERROR, "Erro ao capturar tela")
                    isRunning = false
                    break
                }
                
                addLog("Enviando para IA analisar...")
                val action = liteRTController?.analyzeScreenAndDecide(
                    screenshot!!,
                    task,
                    conversationHistory
                )
                
                if (action == null) {
                    addLog("Erro: IA não retornou ação válida")
                    _taskStatus.value = TaskStatus(Status.ERROR, "Erro na análise da IA")
                    isRunning = false
                    break
                }
                
                addLog("IA decidiu: ${action.type}")
                conversationHistory.add("Ação executada: ${action.type}")
                
                if (action.type.name == "WAIT" && action.amount == null) {
                    addLog("Tarefa concluída!")
                    _taskStatus.value = TaskStatus(Status.COMPLETED, "Tarefa concluída", 100)
                    taskCompleted = true
                    isRunning = false
                    break
                }
                
                _taskStatus.value = TaskStatus(Status.EXECUTING, "Executando: ${action.type}...")
                
                var actionSuccess = false
                accessibilityService.executeAction(action) { success ->
                    actionSuccess = success
                }
                
                delay(1500)
                
                if (!actionSuccess) {
                    addLog("Erro ao executar ação: ${action.type}")
                    _taskStatus.value = TaskStatus(Status.ERROR, "Erro ao executar ação")
                    isRunning = false
                    break
                }
                
                addLog("Ação executada com sucesso")
                
                delay(1000)
            }
            
            if (stepCount >= MAX_STEPS && !taskCompleted) {
                addLog("Limite de passos atingido ($MAX_STEPS)")
                _taskStatus.value = TaskStatus(Status.ERROR, "Limite de passos atingido")
            }
            
            isRunning = false
        }
    }
    
    fun stopTask() {
        if (isRunning) {
            isRunning = false
            addLog("Tarefa interrompida pelo usuário")
            _taskStatus.value = TaskStatus(Status.IDLE, "Tarefa interrompida")
        }
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
        floatingLogOverlay?.clearLogs()
    }
    
    fun setFloatingLogEnabled(enabled: Boolean) {
        if (enabled) {
            floatingLogOverlay?.show()
            addLog("Log flutuante ativado")
        } else {
            floatingLogOverlay?.hide()
            addLog("Log flutuante desativado")
        }
    }
    
    fun isFloatingLogVisible(): Boolean {
        return floatingLogOverlay?.isVisible() ?: false
    }
    
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        Log.d(TAG, message)
        _logs.value = _logs.value + logMessage
        floatingLogOverlay?.addLog(logMessage)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AION Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para o serviço AION AI Controller"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AION AI Controller")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
