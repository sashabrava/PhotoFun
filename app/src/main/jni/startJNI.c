#include "com_cels_photofun_MainActivity.h"

/*
 * Class:     com_cels_photofun_MainActivity
 * Method:    helloNDI
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_cels_photofun_MainActivity_startNDI
  (JNIEnv *env, jobject obj){
return (*env)->NewStringUTF(env, "Hello from JNI !");
  }