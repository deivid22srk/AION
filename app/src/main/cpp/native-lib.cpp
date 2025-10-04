#include <jni.h>
#include <string>
#include <android/log.h>
#include "image_processor.h"

#define LOG_TAG "AION-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_aion_aicontroller_NativeLib_getVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "AION Native Library v1.0";
    LOGI("Native library loaded: %s", version.c_str());
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_aion_aicontroller_NativeLib_optimizeBitmap(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap,
        jint quality) {
    
    LOGI("Optimizing bitmap with quality: %d", quality);
    
    if (bitmap == nullptr) {
        LOGE("Bitmap is null");
        return JNI_FALSE;
    }
    
    bool result = ImageProcessor::optimizeBitmap(env, bitmap, quality);
    
    if (result) {
        LOGI("Bitmap optimization successful");
        return JNI_TRUE;
    } else {
        LOGE("Bitmap optimization failed");
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_aion_aicontroller_NativeLib_calculateImageHash(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap) {
    
    if (bitmap == nullptr) {
        LOGE("Bitmap is null");
        return 0;
    }
    
    jint hash = ImageProcessor::calculateImageHash(env, bitmap);
    LOGI("Image hash calculated: %d", hash);
    
    return hash;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_aion_aicontroller_NativeLib_reduceImageSize(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap,
        jint maxWidth,
        jint maxHeight) {
    
    LOGI("Reducing image size to max: %dx%d", maxWidth, maxHeight);
    
    if (bitmap == nullptr) {
        LOGE("Bitmap is null");
        return JNI_FALSE;
    }
    
    bool result = ImageProcessor::reduceImageSize(env, bitmap, maxWidth, maxHeight);
    
    if (result) {
        LOGI("Image size reduction successful");
        return JNI_TRUE;
    } else {
        LOGE("Image size reduction failed");
        return JNI_FALSE;
    }
}
