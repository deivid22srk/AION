package com.aion.aicontroller.ai

import android.graphics.Bitmap
import android.util.Log
import com.aion.aicontroller.data.AIAction
import com.aion.aicontroller.data.ActionType
import com.google.ai.edge.litert.llm.LlmClient
import com.google.ai.edge.litert.llm.LlmTask
import com.google.ai.edge.litert.llm.Resource
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class LiteRTMultimodalController(
    private val modelPath: String
) {
    private val gson = Gson()
    private var llmTask: LlmTask? = null
    private var isModelLoaded = false
    
    companion object {
        private const val TAG = "LiteRTMultimodal"
    }
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inicializando LiteRT-LM Engine...")
            Log.d(TAG, "Model path: $modelPath")
            
            llmTask = LlmClient.loadModel(modelPath)
            
            isModelLoaded = true
            Log.d(TAG, "✅ LiteRT-LM Engine carregado com sucesso!")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao carregar LiteRT-LM Engine: ${e.message}", e)
            false
        }
    }
    
    suspend fun analyzeScreenAndDecide(
        screenshot: Bitmap,
        task: String,
        conversationHistory: List<String> = emptyList()
    ): AIAction? = withContext(Dispatchers.Default) {
        
        if (!isModelLoaded || llmTask == null) {
            Log.e(TAG, "Modelo não está carregado")
            return@withContext null
        }
        
        try {
            Log.d(TAG, "Analisando tela com visão multimodal...")
            
            val prompt = buildPrompt(task, conversationHistory)
            Log.d(TAG, "Prompt: $prompt")
            
            val imageBytes = screenshot.toPngByteArray()
            
            val responseBuilder = StringBuilder()
            
            llmTask?.generateResponseAsync(
                prompt = prompt,
                images = listOf(imageBytes)
            )?.collect { partialResponse ->
                responseBuilder.append(partialResponse)
                Log.d(TAG, "Token: $partialResponse")
            }
            
            val responseText = responseBuilder.toString().trim()
            Log.d(TAG, "Resposta completa: $responseText")
            
            val action = parseResponseToAction(responseText)
            Log.d(TAG, "Ação decidida: $action")
            
            return@withContext action
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar tela: ${e.message}", e)
            return@withContext null
        }
    }
    
    fun generateResponseStream(
        screenshot: Bitmap,
        prompt: String
    ): Flow<String> = flow {
        if (!isModelLoaded || llmTask == null) {
            throw IllegalStateException("Modelo não está carregado")
        }
        
        val imageBytes = screenshot.toPngByteArray()
        
        llmTask?.generateResponseAsync(
            prompt = prompt,
            images = listOf(imageBytes)
        )?.collect { partialResponse ->
            emit(partialResponse)
        }
    }
    
    private fun buildPrompt(task: String, conversationHistory: List<String>): String {
        val historyText = if (conversationHistory.isNotEmpty()) {
            "\nHistórico de ações:\n${conversationHistory.takeLast(3).joinToString("\n")}\n"
        } else {
            ""
        }
        
        return """
Você é um assistente de IA que controla um dispositivo Android. Analise a imagem da tela e responda em JSON.

Tarefa do usuário: $task
$historyText
Analise a imagem da tela e decida a próxima ação para completar a tarefa.

Responda APENAS com JSON no seguinte formato:
{
  "action": "TIPO_ACAO",
  "target": "elemento alvo (opcional)",
  "x": 500,
  "y": 800,
  "text": "texto a digitar (opcional)",
  "direction": "UP/DOWN/LEFT/RIGHT (para scroll)",
  "reasoning": "explicação da decisão"
}

Tipos de ação disponíveis:
- CLICK: clicar em coordenadas específicas
- TYPE_TEXT: digitar texto (precisa do campo "text")
- SCROLL: rolar a tela (precisa do campo "direction")
- BACK: voltar
- HOME: ir para home
- OPEN_APP: abrir aplicativo (precisa do campo "target")
- WAIT: aguardar
""".trimIndent()
    }
    
    private fun parseResponseToAction(responseText: String): AIAction? {
        try {
            val jsonMatch = Regex("""\{[^}]+\}""").find(responseText)
            val jsonText = jsonMatch?.value ?: responseText
            
            val jsonObject = gson.fromJson(jsonText, Map::class.java)
            
            val actionType = when (jsonObject["action"]?.toString()?.uppercase()) {
                "CLICK" -> ActionType.CLICK
                "TYPE_TEXT" -> ActionType.TYPE_TEXT
                "SCROLL" -> ActionType.SCROLL
                "BACK" -> ActionType.BACK
                "HOME" -> ActionType.HOME
                "OPEN_APP" -> ActionType.OPEN_APP
                "WAIT" -> ActionType.WAIT
                else -> ActionType.WAIT
            }
            
            return AIAction(
                type = actionType,
                target = jsonObject["target"]?.toString(),
                x = (jsonObject["x"] as? Double)?.toInt(),
                y = (jsonObject["y"] as? Double)?.toInt(),
                text = jsonObject["text"]?.toString(),
                direction = jsonObject["direction"]?.toString()
            )
            
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Erro ao parsear JSON: ${e.message}")
            Log.e(TAG, "Resposta: $responseText")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar resposta: ${e.message}", e)
        }
        
        return null
    }
    
    private fun Bitmap.toPngByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
    
    fun unload() {
        try {
            llmTask?.close()
            llmTask = null
            isModelLoaded = false
            Log.d(TAG, "Modelo descarregado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao descarregar modelo: ${e.message}", e)
        }
    }
    
    fun isReady(): Boolean = isModelLoaded
}
