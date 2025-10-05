package com.aion.aicontroller.local

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int
)

class HuggingFaceDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "HFDownloader"
        private const val HF_BASE_URL = "https://huggingface.co"
        private const val CHUNK_SIZE = 8192
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    
    fun downloadModel(
        repoId: String,
        filename: String,
        destination: File
    ): Flow<DownloadProgress> = flow {
        try {
            val url = "$HF_BASE_URL/$repoId/resolve/main/$filename"
            
            Log.d(TAG, "Baixando modelo de: $url")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw Exception("Falha no download: ${response.code}")
            }
            
            val body = response.body ?: throw Exception("Body vazio")
            val totalBytes = body.contentLength()
            
            Log.d(TAG, "Tamanho total: ${totalBytes / 1024 / 1024}MB")
            
            destination.parentFile?.mkdirs()
            
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(destination)
            
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesDownloaded = 0L
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                val percentage = if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt()
                } else {
                    0
                }
                
                emit(DownloadProgress(bytesDownloaded, totalBytes, percentage))
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            Log.d(TAG, "Download concluído: ${destination.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao baixar modelo: ${e.message}", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)
    
    fun checkModelExists(destination: File): Boolean {
        return destination.exists() && destination.length() > 0
    }
    
    fun deleteModel(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar modelo: ${e.message}", e)
            false
        }
    }
}
