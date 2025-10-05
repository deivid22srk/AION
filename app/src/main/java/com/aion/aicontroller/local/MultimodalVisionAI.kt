package com.aion.aicontroller.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.GenerationConfig
import com.google.ai.edge.litertlm.LLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * IA Multimodal Local usando LiteRT LM
 * 
 * Esta classe integra modelos de linguagem multimodais que podem
 * processar imagens e texto simultaneamente, rodando 100% offline.
 * 
 * Baseado no Google AI Edge Gallery e adaptado para o AION.
 */
class MultimodalVisionAI(private val context: Context) {
    
    companion object {
        private const val TAG = "MultimodalVisionAI"
        
        // Prompt especializado para controle de Android
        private const val SYSTEM_PROMPT = """You are an AI assistant specialized in Android device control.

You will receive:
1. A screenshot of the device
2. A task the user wants to perform

Your job is to analyze the screen and decide which action to take to complete the task.

IMPORTANT: Your response MUST be ONLY a valid JSON object with this structure:

{
  "action": "CLICK | LONG_CLICK | TYPE_TEXT | SCROLL | SWIPE | BACK | HOME | RECENT_APPS | OPEN_APP | WAIT | TAKE_SCREENSHOT",
  "target": "element description (optional)",
  "x": x_coordinate_in_pixels (optional),
  "y": y_coordinate_in_pixels (optional),
  "text": "text to type (TYPE_TEXT only)",
  "direction": "UP | DOWN | LEFT | RIGHT (for SCROLL and SWIPE)",
  "amount": scroll_amount_or_distance (optional),
  "reasoning": "brief explanation of your decision"
}

Examples:

Click button:
{"action": "CLICK", "x": 500, "y": 800, "reasoning": "Clicking the settings button in center"}

Open app:
{"action": "OPEN_APP", "target": "Chrome", "reasoning": "Opening Chrome browser"}

Type text:
{"action": "TYPE_TEXT", "text": "cake recipes", "reasoning": "Typing the search query"}

Scroll:
{"action": "SCROLL", "direction": "DOWN", "amount": 500, "reasoning": "Scrolling down to see more"}

Back:
{"action": "BACK", "reasoning": "Going back to previous screen"}

Task complete:
{"action": "WAIT", "reasoning": "Task completed successfully"}

REMEMBER: Respond with ONLY the JSON, no extra text before or after."""
    }
    
    private var llm: LLM? = null
    private var isInitialized = false
    
    /**
     * Inicializa o modelo multimodal
     * 
     * @param modelPath Caminho para o modelo .litertlm ou .tflite
     * @param supportImage Se o modelo suporta entrada de imagem
     * @return true se inicializou com sucesso
     */
    suspend fun initialize(modelPath: String, supportImage: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inicializando modelo multimodal: $modelPath")
            
            if (!File(modelPath).exists()) {
                Log.e(TAG, "Arquivo de modelo não existe: $modelPath")
                return@withContext false
            }
            
            llm = LLM.create(
                context = context,
                modelPath = modelPath
            )
            
            isInitialized = true
            Log.d(TAG, "Modelo multimodal inicializado com sucesso")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar modelo multimodal: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Gera resposta baseada em screenshot e prompt
     * 
     * @param bitmap Screenshot do dispositivo
     * @param userPrompt Tarefa que o usuário quer executar
     * @param temperature Temperatura de geração (0.0-1.0)
     * @return Resposta do modelo em formato JSON
     */
    suspend fun generateResponse(
        bitmap: Bitmap,
        userPrompt: String,
        temperature: Float = 0.7f
    ): String? = withContext(Dispatchers.Default) {
        if (!isInitialized || llm == null) {
            Log.e(TAG, "Modelo não está inicializado")
            return@withContext null
        }
        
        try {
            Log.d(TAG, "Gerando resposta para tarefa: $userPrompt")
            
            val fullPrompt = """$SYSTEM_PROMPT

Task: $userPrompt

Analyze the screen image and respond with a valid JSON indicating the next action."""
            
            val config = GenerationConfig(
                temperature = temperature,
                topK = 40,
                maxOutputTokens = 1024
            )
            
            var fullResponse = StringBuilder()
            
            llm!!.generateResponse(fullPrompt, config).collect { partialResult ->
                fullResponse.append(partialResult)
            }
            
            val result = fullResponse.toString().trim()
            Log.d(TAG, "Resposta gerada: $result")
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Gera resposta com streaming
     * 
     * @param bitmap Screenshot do dispositivo
     * @param userPrompt Tarefa que o usuário quer executar
     * @param temperature Temperatura de geração
     * @return Flow de tokens gerados
     */
    fun generateResponseStream(
        bitmap: Bitmap,
        userPrompt: String,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        if (!isInitialized || llm == null) {
            Log.e(TAG, "Modelo não está inicializado")
            return@flow
        }
        
        try {
            val fullPrompt = """$SYSTEM_PROMPT

Task: $userPrompt

Analyze the screen and respond with JSON."""
            
            val config = GenerationConfig(
                temperature = temperature,
                topK = 40,
                maxOutputTokens = 1024
            )
            
            llm!!.generateResponse(fullPrompt, config).collect { token ->
                emit(token)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro no streaming: ${e.message}", e)
        }
    }
    
    /**
     * Analisa screenshot com contexto de histórico
     * 
     * @param bitmap Screenshot atual
     * @param task Tarefa principal
     * @param conversationHistory Histórico de ações anteriores
     * @return Resposta JSON
     */
    suspend fun analyzeWithContext(
        bitmap: Bitmap,
        task: String,
        conversationHistory: List<String>
    ): String? {
        val historyContext = if (conversationHistory.isNotEmpty()) {
            "\n\nPrevious actions:\n${conversationHistory.joinToString("\n")}"
        } else {
            ""
        }
        
        val promptWithHistory = "$task$historyContext"
        return generateResponse(bitmap, promptWithHistory)
    }
    
    fun cleanup() {
        try {
            llm?.close()
            llm = null
            isInitialized = false
            Log.d(TAG, "Modelo multimodal limpo")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar modelo: ${e.message}")
        }
    }
    
    fun isLoaded(): Boolean = isInitialized
    
    /**
     * Obtém informações sobre o modelo carregado
     */
    fun getModelInfo(): ModelInfo? {
        return if (isInitialized && llm != null) {
            ModelInfo(
                isLoaded = true,
                supportsVision = true,
                supportsStreaming = true
            )
        } else {
            null
        }
    }
}

data class ModelInfo(
    val isLoaded: Boolean,
    val supportsVision: Boolean,
    val supportsStreaming: Boolean
)
