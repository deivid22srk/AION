package com.aion.aicontroller.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaPipeVisionInference(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    private var currentModelPath: String? = null
    private val TAG = "MediaPipeVision"
    
    data class ModelConfig(
        val modelPath: String,
        val maxTokens: Int = 1024,
        val temperature: Float = 0.8f,
        val topK: Int = 40,
        val randomSeed: Int = 101
    )
    
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig(modelPath)): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            unloadModel()
            
            val modelFile = File(modelPath)
            if (!modelFile.exists()) {
                return@withContext Result.failure(Exception("Arquivo do modelo não encontrado: $modelPath"))
            }
            
            Log.d(TAG, "Carregando modelo MediaPipe: $modelPath")
            Log.d(TAG, "Tamanho do arquivo: ${modelFile.length() / 1024 / 1024} MB")
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(config.maxTokens)
                .setTopK(config.topK)
                .setTemperature(config.temperature)
                .setRandomSeed(config.randomSeed)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            currentModelPath = modelPath
            
            Log.d(TAG, "Modelo MediaPipe carregado com sucesso!")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar modelo MediaPipe: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun generateResponse(
        image: Bitmap?,
        prompt: String,
        temperature: Float = 0.8f
    ): Result<String> = withContext(Dispatchers.Default) {
        return@withContext try {
            val inference = llmInference ?: return@withContext Result.failure(
                Exception("Modelo não carregado. Carregue um modelo primeiro.")
            )
            
            Log.d(TAG, "Gerando resposta para prompt: $prompt")
            
            val fullPrompt = if (image != null) {
                buildMultimodalPrompt(prompt, image)
            } else {
                buildTextPrompt(prompt)
            }
            
            Log.d(TAG, "Prompt completo criado (${fullPrompt.length} caracteres)")
            
            val response = inference.generateResponse(fullPrompt)
            
            Log.d(TAG, "Resposta gerada: ${response.take(100)}...")
            Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun generateResponseAsync(
        image: Bitmap?,
        prompt: String,
        temperature: Float = 0.8f,
        onPartialResult: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.Default) {
        return@withContext try {
            val inference = llmInference ?: return@withContext Result.failure(
                Exception("Modelo não carregado. Carregue um modelo primeiro.")
            )
            
            val fullPrompt = if (image != null) {
                buildMultimodalPrompt(prompt, image)
            } else {
                buildTextPrompt(prompt)
            }
            
            Log.d(TAG, "Gerando resposta assíncrona...")
            
            val fullResponse = StringBuilder()
            
            inference.generateResponseAsync(fullPrompt).use { responseStream ->
                for (partialResult in responseStream) {
                    fullResponse.append(partialResult)
                    onPartialResult(partialResult)
                }
            }
            
            Log.d(TAG, "Resposta assíncrona completa: ${fullResponse.length} caracteres")
            Result.success(fullResponse.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta assíncrona: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun buildMultimodalPrompt(prompt: String, image: Bitmap): String {
        return """
            Você é um assistente de IA especializado em controlar dispositivos Android.
            Analise a imagem fornecida da tela do dispositivo e responda ao seguinte comando:
            
            Comando: $prompt
            
            IMPORTANTE: Responda APENAS com um objeto JSON no seguinte formato:
            {
              "action": "TIPO_DE_AÇÃO",
              "target": "alvo opcional",
              "x": coordenada_x_opcional,
              "y": coordenada_y_opcional,
              "text": "texto opcional",
              "direction": "direção opcional",
              "amount": valor_opcional,
              "reasoning": "explicação breve da sua decisão"
            }
            
            Tipos de ações disponíveis:
            - OPEN_APP: abrir aplicativo (use "target" com o nome do app)
            - CLICK: clicar em coordenadas (use "x" e "y")
            - TYPE_TEXT: digitar texto (use "text")
            - SCROLL: rolar tela (use "direction": "UP"/"DOWN" e "amount")
            - BACK: voltar
            - HOME: ir para home
            - WAIT: esperar/tarefa completa
            
            Responda APENAS com o JSON, sem texto adicional antes ou depois.
        """.trimIndent()
    }
    
    private fun buildTextPrompt(prompt: String): String {
        return """
            Você é um assistente de IA especializado em controlar dispositivos Android.
            Responda ao seguinte comando:
            
            Comando: $prompt
            
            IMPORTANTE: Responda APENAS com um objeto JSON no seguinte formato:
            {
              "action": "TIPO_DE_AÇÃO",
              "target": "alvo opcional",
              "text": "texto opcional",
              "reasoning": "explicação breve da sua decisão"
            }
            
            Tipos de ações disponíveis:
            - OPEN_APP: abrir aplicativo (use "target" com o nome do app)
            - TYPE_TEXT: digitar texto (use "text")
            - SCROLL: rolar tela
            - BACK: voltar
            - HOME: ir para home
            - WAIT: esperar/tarefa completa
            
            Responda APENAS com o JSON, sem texto adicional.
        """.trimIndent()
    }
    
    fun unloadModel() {
        try {
            llmInference?.close()
            llmInference = null
            currentModelPath = null
            Log.d(TAG, "Modelo MediaPipe descarregado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao descarregar modelo: ${e.message}", e)
        }
    }
    
    fun isModelLoaded(): Boolean = llmInference != null
    
    fun getCurrentModelPath(): String? = currentModelPath
    
    fun getSizeInTokens(text: String): Int {
        return try {
            llmInference?.sizeInTokens(text) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao calcular tokens: ${e.message}")
            0
        }
    }
}
