package com.aion.aicontroller

import android.graphics.Bitmap

object NativeLib {
    
    init {
        System.loadLibrary("aion_native")
    }
    
    external fun getVersion(): String
    
    external fun optimizeBitmap(bitmap: Bitmap, quality: Int): Boolean
    
    external fun calculateImageHash(bitmap: Bitmap): Int
    
    external fun reduceImageSize(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Boolean
}
