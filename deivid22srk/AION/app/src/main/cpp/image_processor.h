#ifndef AION_IMAGE_PROCESSOR_H
#define AION_IMAGE_PROCESSOR_H

#include <jni.h>
#include <android/bitmap.h>

class ImageProcessor {
public:
    static bool optimizeBitmap(JNIEnv* env, jobject bitmap, int quality);
    static jint calculateImageHash(JNIEnv* env, jobject bitmap);
    static bool reduceImageSize(JNIEnv* env, jobject bitmap, int maxWidth, int maxHeight);
    
private:
    static bool lockBitmap(JNIEnv* env, jobject bitmap, AndroidBitmapInfo* info, void** pixels);
    static void unlockBitmap(JNIEnv* env, jobject bitmap);
};

#endif
