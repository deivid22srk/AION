package com.aion.aicontroller

import android.graphics.Bitmap

class NativeLib {
    
    init {
        System.loadLibrary("aion_native")
    }
    
    external fun getVersion(): String
    
    external fun optimizeBitmap(bitmap: Bitmap, quality: Int): Boolean
    
    external fun calculateImageHash(bitmap: Bitmap): Int
    
    external fun reduceImageSize(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Boolean
    
    external fun loadVisionModel(modelPath: String, mmProjPath: String): Boolean
    
    external fun unloadVisionModel()
    
    external fun generateVisionResponse(
        imagePath: String, 
        prompt: String, 
        temperature: Float, 
        maxTokens: Int
    ): String?
}
