#include "com_cels_photofun_MainActivity.h"
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
/*
 * Class:     com_cels_photofun_MainActivity
 * Method:    buildSegmented
 */
 #define  LOG_TAG    "DEBUG"
 #define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
 #define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
 JNIEXPORT jobject JNICALL Java_com_cels_photofun_MainActivity_buildSegmented
   (JNIEnv *env, jobject obj, jobject bitmap){
    LOGD("Segmentation starts to work");
  AndroidBitmapInfo info;
  int ret;
   LOGD("AndroidBitmapInfo created");
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0)
      {
      LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
      return NULL;
      }
    LOGD("width:%d height:%d stride:%d", info.width, info.height, info.stride);
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
      {
      LOGE("Bitmap format is not RGBA_8888!");
      return NULL;
      }
      return NULL;
   }
/*JNIEXPORT jstring JNICALL Java_com_cels_photofun_MainActivity_startNDI
  (JNIEnv *env, jobject obj){
return (*env)->NewStringUTF(env, "Hello from JNI !");
  }*/