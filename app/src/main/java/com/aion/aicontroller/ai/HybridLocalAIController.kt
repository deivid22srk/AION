package com.aion.aicontroller.ai

import android.graphics.Bitmap
import android.util.Log
import com.aion.aicontroller.data.AIAction
import com.aion.aicontroller.data.ActionType
import com.aion.aicontroller.local.MultimodalVisionAI
import com.aion.aicontroller.local.ScreenElement
import com.aion.aicontroller.local.TFLiteVisionInference
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controlador Híbrido de IA Local
 * 
 * Combina:
 * 1. TFLite para detecção de elementos UI (rápido e leve)
 * 2. LiteRT LM para decisões multimodais (preciso e inteligente)
 * 
 * Funciona 100% offline e otimizado para Android.
 */
class HybridLocalAIController(
    private val visionInference: TFLiteVisionInference,
    private val multimodalAI: MultimodalVisionAI
) {
    private val gson = Gson()
    
    companion object {
        private const val TAG = "HybridLocalAIController"
        
        // Prompt simplificado para modelo multimodal
        private const val DECISION_PROMPT_TEMPLATE = """Based on the screen analysis below, decide the next action.

Screen Analysis:
%s

Task: %s

Respond with JSON action."""
    }
    
    /**
     * Modo de análise
     */
    enum class AnalysisMode {
        VISION_ONLY,       // Apenas detecção de elementos (mais rápido)
        MULTIMODAL_ONLY,   // Apenas modelo multimodal (mais preciso)
        HYBRID             // Combina ambos (balanceado)
    }
    
    /**
     * Analisa tela e decide próxima ação
     * 
     * @param screenshot Screenshot do dispositivo
     * @param task Tarefa que o usuário quer executar
     * @param mode Modo de análise (HYBRID por padrão)
     * @param conversationHistory Histórico de ações
     * @return Ação a ser executada
     */
    suspend fun analyzeScreenAndDecide(
        screenshot: Bitmap,
        task: String,
        mode: AnalysisMode = AnalysisMode.HYBRID,
        conversationHistory: List<String> = emptyList()
    ): AIAction? = withContext(Dispatchers.Default) {
        
        try {
            Log.d(TAG, "Analisando tela em modo: $mode")
            
            return@withContext when (mode) {
                AnalysisMode.VISION_ONLY -> {
                    analyzeWithVisionOnly(screenshot, task)
                }
                AnalysisMode.MULTIMODAL_ONLY -> {
                    analyzeWithMultimodalOnly(screenshot, task, conversationHistory)
                }
                AnalysisMode.HYBRID -> {
                    analyzeHybrid(screenshot, task, conversationHistory)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar tela: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Análise apenas com visão computacional
     * Mais rápido, usa regras para decisões simples
     */
    private suspend fun analyzeWithVisionOnly(
        screenshot: Bitmap,
        task: String
    ): AIAction? {
        Log.d(TAG, "Modo: Apenas Visão")
        
        val analysis = visionInference.analyzeScreenshot(screenshot)
        Log.d(TAG, analysis.description)
        
        return createActionFromElements(analysis.elements, task)
    }
    
    /**
     * Análise apenas com modelo multimodal
     * Mais preciso, mas mais lento
     */
    private suspend fun analyzeWithMultimodalOnly(
        screenshot: Bitmap,
        task: String,
        conversationHistory: List<String>
    ): AIAction? {
        Log.d(TAG, "Modo: Apenas Multimodal")
        
        val response = multimodalAI.analyzeWithContext(
            bitmap = screenshot,
            task = task,
            conversationHistory = conversationHistory
        )
        
        return if (response != null) {
            parseAIResponse(response)
        } else {
            null
        }
    }
    
    /**
     * Análise híbrida (recomendado)
     * Usa visão para contexto e multimodal para decisão
     */
    private suspend fun analyzeHybrid(
        screenshot: Bitmap,
        task: String,
        conversationHistory: List<String>
    ): AIAction? {
        Log.d(TAG, "Modo: Híbrido")
        
        val analysis = visionInference.analyzeScreenshot(screenshot)
        Log.d(TAG, "Análise da tela: ${analysis.description}")
        
        if (analysis.elements.isEmpty()) {
            Log.w(TAG, "Nenhum elemento detectado, usando multimodal direto")
            return analyzeWithMultimodalOnly(screenshot, task, conversationHistory)
        }
        
        val enrichedPrompt = String.format(
            DECISION_PROMPT_TEMPLATE,
            analysis.description,
            task
        )
        
        val response = multimodalAI.generateResponse(
            bitmap = screenshot,
            userPrompt = enrichedPrompt,
            temperature = 0.7f
        )
        
        return if (response != null) {
            val action = parseAIResponse(response)
            
            if (action != null && action.type == ActionType.CLICK && action.x == null) {
                val target = action.target ?: ""
                val element = visionInference.findBestMatch(analysis.elements, target)
                
                if (element != null) {
                    Log.d(TAG, "Enriquecendo ação com coordenadas do elemento: ${element.label}")
                    return@analyzeHybrid action.copy(
                        x = element.boundingBox.centerX(),
                        y = element.boundingBox.centerY()
                    )
                }
            }
            
            action
        } else {
            null
        }
    }
    
    /**
     * Cria ação baseada em elementos detectados (fallback)
     */
    private fun createActionFromElements(
        elements: List<ScreenElement>,
        task: String
    ): AIAction? {
        if (elements.isEmpty()) {
            return AIAction(
                type = ActionType.WAIT,
                target = "Nenhum elemento detectado"
            )
        }
        
        val taskLower = task.lowercase()
        
        return when {
            taskLower.contains("abrir") || taskLower.contains("open") -> {
                val appName = extractAppName(task)
                AIAction(
                    type = ActionType.OPEN_APP,
                    target = appName
                )
            }
            
            taskLower.contains("voltar") || taskLower.contains("back") -> {
                AIAction(type = ActionType.BACK)
            }
            
            taskLower.contains("rolar") || taskLower.contains("scroll") -> {
                val direction = when {
                    taskLower.contains("baixo") || taskLower.contains("down") -> "DOWN"
                    taskLower.contains("cima") || taskLower.contains("up") -> "UP"
                    else -> "DOWN"
                }
                AIAction(
                    type = ActionType.SCROLL,
                    direction = direction,
                    amount = 500
                )
            }
            
            else -> {
                val bestElement = elements.maxByOrNull { it.confidence }
                if (bestElement != null) {
                    AIAction(
                        type = ActionType.CLICK,
                        target = bestElement.label,
                        x = bestElement.boundingBox.centerX(),
                        y = bestElement.boundingBox.centerY()
                    )
                } else {
                    null
                }
            }
        }
    }
    
    private fun extractAppName(task: String): String {
        val words = task.split(" ")
        val openIndex = words.indexOfFirst { 
            it.lowercase() == "abrir" || it.lowercase() == "open" 
        }
        
        return if (openIndex >= 0 && openIndex < words.size - 1) {
            words[openIndex + 1].replace(Regex("[^a-zA-Z0-9]"), "")
        } else {
            "Chrome"
        }
    }
    
    /**
     * Faz parse da resposta JSON do modelo
     */
    private fun parseAIResponse(response: String): AIAction? {
        try {
            Log.d(TAG, "Fazendo parse da resposta...")
            
            val cleanedResponse = response.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            val jsonStart = cleanedResponse.indexOf("{")
            val jsonEnd = cleanedResponse.lastIndexOf("}") + 1
            
            val jsonOnly = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanedResponse.substring(jsonStart, jsonEnd)
            } else {
                cleanedResponse
            }
            
            Log.d(TAG, "JSON extraído: $jsonOnly")
            
            val jsonResponse = gson.fromJson(jsonOnly, AIResponse::class.java)
            
            val actionType = try {
                ActionType.valueOf(jsonResponse.action.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Tipo de ação inválido: ${jsonResponse.action}")
                return null
            }
            
            val action = AIAction(
                type = actionType,
                target = jsonResponse.target,
                x = jsonResponse.x,
                y = jsonResponse.y,
                text = jsonResponse.text,
                direction = jsonResponse.direction,
                amount = jsonResponse.amount
            )
            
            Log.d(TAG, "Ação criada: $action")
            return action
            
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Erro ao fazer parse do JSON: ${e.message}")
            Log.e(TAG, "Resposta: $response")
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado: ${e.message}", e)
        }
        
        return null
    }
    
    private data class AIResponse(
        val action: String,
        val target: String? = null,
        val x: Int? = null,
        val y: Int? = null,
        val text: String? = null,
        val direction: String? = null,
        val amount: Int? = null,
        val reasoning: String? = null
    )
    
    /**
     * Obtém status dos modelos
     */
    fun getStatus(): ModelStatus {
        return ModelStatus(
            visionLoaded = visionInference.isLoaded(),
            multimodalLoaded = multimodalAI.isLoaded(),
            recommendedMode = when {
                visionInference.isLoaded() && multimodalAI.isLoaded() -> AnalysisMode.HYBRID
                multimodalAI.isLoaded() -> AnalysisMode.MULTIMODAL_ONLY
                visionInference.isLoaded() -> AnalysisMode.VISION_ONLY
                else -> null
            }
        )
    }
    
    data class ModelStatus(
        val visionLoaded: Boolean,
        val multimodalLoaded: Boolean,
        val recommendedMode: AnalysisMode?
    )
}
