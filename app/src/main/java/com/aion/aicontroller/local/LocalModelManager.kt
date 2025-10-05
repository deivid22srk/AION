package com.aion.aicontroller.local

import android.content.Context
import android.util.Log
import com.aion.aicontroller.data.LocalVisionModel
import kotlinx.coroutines.flow.Flow
import java.io.File

class LocalModelManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LocalModelManager"
        private const val MODELS_DIR = "vision_models"
    }
    
    private val downloader = HuggingFaceDownloader(context)
    private val modelsDir = File(context.filesDir, MODELS_DIR)
    
    init {
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }
    
    fun downloadModel(model: LocalVisionModel): Flow<DownloadProgress> {
        val modelFile = getModelFile(model)
        
        return downloader.downloadModel(
            repoId = model.repoId,
            filename = model.modelFilename,
            destination = modelFile
        )
    }
    
    fun downloadMMProj(model: LocalVisionModel): Flow<DownloadProgress> {
        val mmProjFile = getMMProjFile(model)
        
        return downloader.downloadModel(
            repoId = model.repoId,
            filename = model.mmProjFilename,
            destination = mmProjFile
        )
    }
    
    fun isModelDownloaded(model: LocalVisionModel): Boolean {
        val modelFile = getModelFile(model)
        
        if (model.isLiteRT) {
            return downloader.checkModelExists(modelFile)
        }
        
        val mmProjFile = getMMProjFile(model)
        return downloader.checkModelExists(modelFile) && 
               downloader.checkModelExists(mmProjFile)
    }
    
    fun getModelFile(model: LocalVisionModel): File {
        return File(modelsDir, model.modelFilename)
    }
    
    fun getMMProjFile(model: LocalVisionModel): File {
        return File(modelsDir, model.mmProjFilename)
    }
    
    fun deleteModel(model: LocalVisionModel): Boolean {
        val modelFile = getModelFile(model)
        
        if (model.isLiteRT) {
            return downloader.deleteModel(modelFile)
        }
        
        val mmProjFile = getMMProjFile(model)
        return downloader.deleteModel(modelFile) && downloader.deleteModel(mmProjFile)
    }
    
    fun getDownloadedModels(allModels: List<LocalVisionModel>): List<LocalVisionModel> {
        return allModels.filter { isModelDownloaded(it) }
    }
    
    fun getModelSize(model: LocalVisionModel): Long {
        val modelFile = getModelFile(model)
        
        return if (isModelDownloaded(model)) {
            if (model.isLiteRT) {
                modelFile.length()
            } else {
                val mmProjFile = getMMProjFile(model)
                modelFile.length() + mmProjFile.length()
            }
        } else {
            0L
        }
    }
    
    fun getTotalModelsSize(): Long {
        return modelsDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeInBytes bytes"
        }
    }
}
