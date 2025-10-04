package com.aion.aicontroller.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aion.aicontroller.NativeLib
import java.io.File
import java.io.FileOutputStream

class LocalVisionInference(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalVisionInference"
        private const val TEMP_IMAGE_NAME = "temp_image.jpg"
    }
    
    private var isModelLoaded = false
    private val nativeLib = NativeLib()
    
    fun loadModel(modelPath: String, mmProjPath: String): Boolean {
        return try {
            Log.d(TAG, "Carregando modelo: $modelPath")
            Log.d(TAG, "Carregando mmproj: $mmProjPath")
            
            val result = nativeLib.loadVisionModel(modelPath, mmProjPath)
            
            if (result) {
                isModelLoaded = true
                Log.d(TAG, "Modelo carregado com sucesso")
            } else {
                Log.e(TAG, "Falha ao carregar modelo")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar modelo: ${e.message}", e)
            false
        }
    }
    
    fun unloadModel() {
        if (isModelLoaded) {
            try {
                nativeLib.unloadVisionModel()
                isModelLoaded = false
                Log.d(TAG, "Modelo descarregado")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao descarregar modelo: ${e.message}", e)
            }
        }
    }
    
    fun generateResponse(image: Bitmap, prompt: String, temperature: Float = 0.7f): String? {
        if (!isModelLoaded) {
            Log.e(TAG, "Modelo não está carregado")
            return null
        }
        
        return try {
            val imagePath = saveImageTemp(image)
            
            Log.d(TAG, "Gerando resposta para prompt: $prompt")
            Log.d(TAG, "Imagem temporária: $imagePath")
            
            val response = nativeLib.generateVisionResponse(
                imagePath = imagePath,
                prompt = prompt,
                temperature = temperature,
                maxTokens = 4000
            )
            
            deleteImageTemp(imagePath)
            
            Log.d(TAG, "Resposta gerada com sucesso")
            response
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar resposta: ${e.message}", e)
            null
        }
    }
    
    private fun saveImageTemp(bitmap: Bitmap): String {
        val tempFile = File(context.cacheDir, TEMP_IMAGE_NAME)
        
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        return tempFile.absolutePath
    }
    
    private fun deleteImageTemp(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao deletar imagem temporária: ${e.message}")
        }
    }
    
    fun isLoaded(): Boolean = isModelLoaded
}
