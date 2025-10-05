package com.aion.aicontroller.ai

import android.graphics.Bitmap
import android.util.Log
import com.aion.aicontroller.data.AIAction
import com.aion.aicontroller.data.ActionType
import com.aion.aicontroller.local.ScreenElement
import com.aion.aicontroller.local.TFLiteVisionInference
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controlador de IA Local com Visão Computacional
 * 
 * Usa TensorFlow Lite para detectar elementos UI e tomar decisões
 * baseadas em análise visual real da tela.
 * 
 * Funciona 100% offline e otimizado para Android.
 */
class VisionAIController(
    private val visionInference: TFLiteVisionInference
) {
    private val gson = Gson()
    
    companion object {
        private const val TAG = "VisionAIController"
    }
    
    /**
     * Analisa tela e decide próxima ação
     * 
     * @param screenshot Screenshot do dispositivo
     * @param task Tarefa que o usuário quer executar
     * @param conversationHistory Histórico de ações
     * @return Ação a ser executada
     */
    suspend fun analyzeScreenAndDecide(
        screenshot: Bitmap,
        task: String,
        conversationHistory: List<String> = emptyList()
    ): AIAction? = withContext(Dispatchers.Default) {
        
        try {
            Log.d(TAG, "Analisando tela com visão computacional...")
            
            val analysis = visionInference.analyzeScreenshot(screenshot)
            Log.d(TAG, analysis.description)
            
            val action = createActionFromAnalysis(analysis.elements, task)
            
            Log.d(TAG, "Ação decidida: $action")
            return@withContext action
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar tela: ${e.message}", e)
            return@withContext null
        }
    }
    
    /**
     * Cria ação baseada em elementos detectados e tarefa
     */
    private fun createActionFromAnalysis(
        elements: List<ScreenElement>,
        task: String
    ): AIAction? {
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
                    taskLower.contains("esquerda") || taskLower.contains("left") -> "LEFT"
                    taskLower.contains("direita") || taskLower.contains("right") -> "RIGHT"
                    else -> "DOWN"
                }
                AIAction(
                    type = ActionType.SCROLL,
                    direction = direction,
                    amount = 500
                )
            }
            
            taskLower.contains("pesquisar") || taskLower.contains("buscar") || 
            taskLower.contains("digitar") || taskLower.contains("escrever") -> {
                val text = extractTextFromTask(task)
                AIAction(
                    type = ActionType.TYPE_TEXT,
                    text = text
                )
            }
            
            taskLower.contains("clicar") || taskLower.contains("click") ||
            taskLower.contains("tocar") || taskLower.contains("tap") -> {
                val target = extractTarget(task)
                val element = visionInference.findBestMatch(elements, target)
                
                if (element != null) {
                    AIAction(
                        type = ActionType.CLICK,
                        target = element.label,
                        x = element.boundingBox.centerX(),
                        y = element.boundingBox.centerY()
                    )
                } else if (elements.isNotEmpty()) {
                    val bestElement = elements.first()
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
            
            taskLower.contains("home") || taskLower.contains("início") -> {
                AIAction(type = ActionType.HOME)
            }
            
            taskLower.contains("recent") || taskLower.contains("recentes") -> {
                AIAction(type = ActionType.RECENT_APPS)
            }
            
            else -> {
                if (elements.isNotEmpty()) {
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
                } else {
                    AIAction(
                        type = ActionType.WAIT,
                        target = "Aguardando próxima instrução"
                    )
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
    
    private fun extractTextFromTask(task: String): String {
        val keywords = listOf("pesquisar", "buscar", "digitar", "escrever", "search", "type", "por", "for")
        val words = task.split(" ")
        
        val keywordIndex = words.indexOfFirst { word ->
            keywords.any { keyword -> word.lowercase().contains(keyword) }
        }
        
        return if (keywordIndex >= 0 && keywordIndex < words.size - 1) {
            words.drop(keywordIndex + 1).joinToString(" ").trim()
        } else {
            task
        }
    }
    
    private fun extractTarget(task: String): String {
        val keywords = listOf("clicar", "tocar", "click", "tap", "em", "no", "na", "in", "on")
        val words = task.split(" ")
        
        val keywordIndex = words.indexOfFirst { word ->
            keywords.any { keyword -> word.lowercase() == keyword }
        }
        
        return if (keywordIndex >= 0 && keywordIndex < words.size - 1) {
            words.drop(keywordIndex + 1).joinToString(" ").trim()
        } else {
            "button"
        }
    }
    
    /**
     * Obtém status do modelo
     */
    fun isReady(): Boolean = visionInference.isLoaded()
}
