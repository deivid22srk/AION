#include <jni.h>
#include <string>
#include <android/log.h>
#include "image_processor.h"
#include "llama_bridge.h"
#include <memory>

#define LOG_TAG "AION-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static std::unique_ptr<LlamaBridge> g_llamaBridge = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_aion_aicontroller_NativeLib_getVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "AION Native Library v2.0 - LlamaBridge Edition";
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

extern "C" JNIEXPORT jboolean JNICALL
Java_com_aion_aicontroller_NativeLib_loadVisionModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath,
        jstring mmProjPath) {
    
    const char* modelPathStr = env->GetStringUTFChars(modelPath, nullptr);
    const char* mmProjPathStr = env->GetStringUTFChars(mmProjPath, nullptr);
    
    LOGI("Loading vision model via LlamaBridge");
    LOGI("Model path: %s", modelPathStr);
    LOGI("MMProj path: %s", mmProjPathStr);
    
    if (g_llamaBridge == nullptr) {
        g_llamaBridge = std::make_unique<LlamaBridge>();
    }
    
    bool success = g_llamaBridge->loadModel(
        std::string(modelPathStr),
        std::string(mmProjPathStr)
    );
    
    env->ReleaseStringUTFChars(modelPath, modelPathStr);
    env->ReleaseStringUTFChars(mmProjPath, mmProjPathStr);
    
    if (success) {
        LOGI("Vision model loaded successfully");
        return JNI_TRUE;
    } else {
        LOGE("Failed to load vision model");
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_aion_aicontroller_NativeLib_unloadVisionModel(
        JNIEnv* env,
        jobject /* this */) {
    
    if (g_llamaBridge != nullptr) {
        LOGI("Unloading vision model via LlamaBridge");
        g_llamaBridge->unloadModel();
        g_llamaBridge.reset();
        LOGI("Vision model unloaded");
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_aion_aicontroller_NativeLib_generateVisionResponse(
        JNIEnv* env,
        jobject /* this */,
        jstring imagePath,
        jstring prompt,
        jfloat temperature,
        jint maxTokens) {
    
    if (g_llamaBridge == nullptr || !g_llamaBridge->isLoaded()) {
        LOGE("Vision model not loaded");
        return nullptr;
    }
    
    const char* imagePathStr = env->GetStringUTFChars(imagePath, nullptr);
    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    
    LOGI("Generating vision response via LlamaBridge");
    LOGI("Image: %s", imagePathStr);
    LOGI("Prompt length: %zu chars", strlen(promptStr));
    LOGI("Temperature: %.2f, MaxTokens: %d", temperature, maxTokens);
    
    std::string response = g_llamaBridge->generateResponse(
        std::string(imagePathStr),
        std::string(promptStr),
        temperature,
        maxTokens
    );
    
    env->ReleaseStringUTFChars(imagePath, imagePathStr);
    env->ReleaseStringUTFChars(prompt, promptStr);
    
    if (response.empty()) {
        LOGE("Empty response from LlamaBridge");
        return nullptr;
    }
    
    LOGI("Response generated successfully");
    
    return env->NewStringUTF(response.c_str());
}
