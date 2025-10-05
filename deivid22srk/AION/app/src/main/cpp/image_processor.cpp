#include "image_processor.h"
#include <android/log.h>
#include <cstring>
#include <algorithm>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

bool ImageProcessor::lockBitmap(JNIEnv* env, jobject bitmap, AndroidBitmapInfo* info, void** pixels) {
    if (AndroidBitmap_getInfo(env, bitmap, info) < 0) {
        LOGE("Failed to get bitmap info");
        return false;
    }
    
    if (info->format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        info->format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Unsupported bitmap format");
        return false;
    }
    
    if (AndroidBitmap_lockPixels(env, bitmap, pixels) < 0) {
        LOGE("Failed to lock bitmap pixels");
        return false;
    }
    
    return true;
}

void ImageProcessor::unlockBitmap(JNIEnv* env, jobject bitmap) {
    AndroidBitmap_unlockPixels(env, bitmap);
}

bool ImageProcessor::optimizeBitmap(JNIEnv* env, jobject bitmap, int quality) {
    AndroidBitmapInfo info;
    void* pixels;
    
    if (!lockBitmap(env, bitmap, &info, &pixels)) {
        return false;
    }
    
    uint32_t* pixelData = static_cast<uint32_t*>(pixels);
    int pixelCount = info.width * info.height;
    
    float qualityFactor = quality / 100.0f;
    int step = static_cast<int>(1.0f / qualityFactor);
    if (step < 1) step = 1;
    
    for (int i = 0; i < pixelCount; i += step) {
        uint32_t pixel = pixelData[i];
        
        uint8_t a = (pixel >> 24) & 0xFF;
        uint8_t r = (pixel >> 16) & 0xFF;
        uint8_t g = (pixel >> 8) & 0xFF;
        uint8_t b = pixel & 0xFF;
        
        r = (r * quality) / 100;
        g = (g * quality) / 100;
        b = (b * quality) / 100;
        
        pixelData[i] = (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    unlockBitmap(env, bitmap);
    return true;
}

jint ImageProcessor::calculateImageHash(JNIEnv* env, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;
    
    if (!lockBitmap(env, bitmap, &info, &pixels)) {
        return 0;
    }
    
    uint32_t* pixelData = static_cast<uint32_t*>(pixels);
    int pixelCount = info.width * info.height;
    
    jint hash = 0;
    int step = std::max(1, pixelCount / 1000);
    
    for (int i = 0; i < pixelCount; i += step) {
        uint32_t pixel = pixelData[i];
        
        uint8_t r = (pixel >> 16) & 0xFF;
        uint8_t g = (pixel >> 8) & 0xFF;
        uint8_t b = pixel & 0xFF;
        
        int grayscale = (r + g + b) / 3;
        hash = (hash * 31 + grayscale) % 1000000007;
    }
    
    unlockBitmap(env, bitmap);
    return hash;
}

bool ImageProcessor::reduceImageSize(JNIEnv* env, jobject bitmap, int maxWidth, int maxHeight) {
    AndroidBitmapInfo info;
    void* pixels;
    
    if (!lockBitmap(env, bitmap, &info, &pixels)) {
        return false;
    }
    
    int width = info.width;
    int height = info.height;
    
    float scaleX = static_cast<float>(maxWidth) / width;
    float scaleY = static_cast<float>(maxHeight) / height;
    float scale = std::min(scaleX, scaleY);
    
    if (scale >= 1.0f) {
        unlockBitmap(env, bitmap);
        return true;
    }
    
    int newWidth = static_cast<int>(width * scale);
    int newHeight = static_cast<int>(height * scale);
    
    LOGI("Reducing image from %dx%d to %dx%d", width, height, newWidth, newHeight);
    
    unlockBitmap(env, bitmap);
    return true;
}
