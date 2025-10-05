package com.aion.aicontroller.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.File

/**
 * Classe para inferência de visão local usando TensorFlow Lite
 * 
 * Esta implementação usa TensorFlow Lite para analisar screenshots
 * e detectar elementos na tela de forma completamente offline.
 */
class TFLiteVisionInference(private val context: Context) {
    
    companion object {
        private const val TAG = "TFLiteVisionInference"
    }
    
    private var objectDetector: ObjectDetector? = null
    private var isInitialized = false
    
    /**
     * Inicializa o modelo TFLite de detecção de objetos
     * 
     * @param modelPath Caminho para o arquivo .tflite do modelo
     * @return true se inicializou com sucesso
     */
    suspend fun initialize(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inicializando TFLite Vision com modelo: $modelPath")
            
            if (!File(modelPath).exists()) {
                Log.e(TAG, "Arquivo de modelo não existe: $modelPath")
                return@withContext false
            }
            
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(10)
                .setScoreThreshold(0.3f)
                .build()
            
            objectDetector = ObjectDetector.createFromFileAndOptions(
                context,
                modelPath,
                options
            )
            
            isInitialized = true
            Log.d(TAG, "TFLite Vision inicializado com sucesso")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar TFLite Vision: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Detecta elementos na imagem
     * 
     * @param bitmap Screenshot para análise
     * @return Lista de detecções com posições e labels
     */
    suspend fun detectElements(bitmap: Bitmap): List<ScreenElement> = withContext(Dispatchers.Default) {
        if (!isInitialized || objectDetector == null) {
            Log.e(TAG, "Detector não está inicializado")
            return@withContext emptyList()
        }
        
        try {
            Log.d(TAG, "Detectando elementos na tela...")
            
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val results = objectDetector!!.detect(tensorImage)
            
            val elements = results.map { detection ->
                ScreenElement(
                    label = detection.categories.firstOrNull()?.label ?: "unknown",
                    confidence = detection.categories.firstOrNull()?.score ?: 0f,
                    boundingBox = BoundingBox(
                        left = detection.boundingBox.left,
                        top = detection.boundingBox.top,
                        right = detection.boundingBox.right,
                        bottom = detection.boundingBox.bottom
                    )
                )
            }
            
            Log.d(TAG, "Detectados ${elements.size} elementos")
            return@withContext elements
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao detectar elementos: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Analisa screenshot e gera descrição textual
     * 
     * @param bitmap Screenshot para análise
     * @return Descrição dos elementos detectados
     */
    suspend fun analyzeScreenshot(bitmap: Bitmap): ScreenAnalysis = withContext(Dispatchers.Default) {
        val elements = detectElements(bitmap)
        
        val description = if (elements.isEmpty()) {
            "Nenhum elemento UI detectado na tela"
        } else {
            buildString {
                append("Elementos detectados na tela:\n")
                elements.forEachIndexed { index, element ->
                    append("${index + 1}. ${element.label} (confiança: ${(element.confidence * 100).toInt()}%) ")
                    append("em posição (${element.boundingBox.centerX()}, ${element.boundingBox.centerY()})\n")
                }
            }
        }
        
        return@withContext ScreenAnalysis(
            elements = elements,
            description = description,
            screenWidth = bitmap.width,
            screenHeight = bitmap.height
        )
    }
    
    /**
     * Encontra elemento mais próximo de uma posição ou descrição
     * 
     * @param elements Lista de elementos detectados
     * @param target Descrição do elemento desejado
     * @return Elemento mais relevante ou null
     */
    fun findBestMatch(elements: List<ScreenElement>, target: String): ScreenElement? {
        if (elements.isEmpty()) return null
        
        val targetLower = target.lowercase()
        
        val exactMatch = elements.find { 
            it.label.lowercase().contains(targetLower) || 
            targetLower.contains(it.label.lowercase()) 
        }
        
        if (exactMatch != null) {
            Log.d(TAG, "Encontrado match exato: ${exactMatch.label}")
            return exactMatch
        }
        
        val bestMatch = elements.maxByOrNull { it.confidence }
        Log.d(TAG, "Retornando elemento com maior confiança: ${bestMatch?.label}")
        return bestMatch
    }
    
    fun cleanup() {
        try {
            objectDetector?.close()
            objectDetector = null
            isInitialized = false
            Log.d(TAG, "Recursos limpos")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar recursos: ${e.message}")
        }
    }
    
    fun isLoaded(): Boolean = isInitialized
}

/**
 * Representa um elemento detectado na tela
 */
data class ScreenElement(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

/**
 * Caixa delimitadora de um elemento
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun centerX(): Int = ((left + right) / 2).toInt()
    fun centerY(): Int = ((top + bottom) / 2).toInt()
    fun width(): Float = right - left
    fun height(): Float = bottom - top
}

/**
 * Resultado da análise de uma tela
 */
data class ScreenAnalysis(
    val elements: List<ScreenElement>,
    val description: String,
    val screenWidth: Int,
    val screenHeight: Int
)
