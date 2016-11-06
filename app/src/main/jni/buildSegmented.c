#include "com_cels_photofun_MainActivity.h"
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
typedef struct {
        int id;
        int pixelCount;
        int red;
        int green;
        int blue;
        int reds;
        int greens;
        int blues;
 }Cluster;
/*
 * Class:     com_cels_photofun_MainActivity
 * Method:    buildSegmented
 */
 #define  LOG_TAG    "DEBUG"
 #define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
 #define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
void createClusters(Cluster **clusters, int amount){
 Cluster *x = (Cluster*) malloc(sizeof(Cluster));
 LOGD("small memory got");
 x = (Cluster*) realloc(x, sizeof(Cluster)*amount);
  LOGD("big memory got");
 *clusters = x;
  LOGD("value is gotten");
//*clusters = (Cluster*) malloc(sizeof(Cluster)*amount);
}
void cluster_new(Cluster *cluster)
{
  cluster->id = cluster->pixelCount  =  cluster->red = cluster->green = cluster->blue = cluster->reds = cluster->reds = cluster->greens = cluster->blues = 0;

}
void cluster_with_id(Cluster *cluster, int id)
{
  cluster_new(cluster);
  cluster->id = id;
}
int getRGB(Cluster *cluster) {
           int r = (int) (cluster->reds / cluster->pixelCount);
           int g = (int) (cluster->greens / cluster->pixelCount);
           int b = (int) (cluster->blues /cluster->pixelCount);
            //LOGD("red:%d green:%d blue:%d", r, g, b);
           return 0xff000000 | r << 16 | g << 8 | b;
           //return r+g+b;
       }
void addPixel(Cluster *cluster, int pixel) {

           cluster->reds += (pixel >> 16) & 0xff;
           cluster->greens += (pixel >> 8) & 0xff;
           cluster->blues += pixel & 0xff;
           cluster->pixelCount++;
           cluster->red = (int)(cluster->reds / cluster->pixelCount);
           cluster->green = (int)(cluster->greens / cluster->pixelCount);
           cluster->blue = (int)(cluster->blues / cluster->pixelCount);
       }
void removePixel(Cluster *cluster, int pixel) {
           cluster->reds -= (pixel >> 16) & 0xff;
           cluster->greens -= (pixel >> 8) & 0xff;
           cluster->blues -= pixel & 0xff;
           cluster->pixelCount--;
           cluster->red = (int)(cluster->reds / cluster->pixelCount);
           cluster->green = (int)(cluster->greens / cluster->pixelCount);
           cluster->blue = (int)(cluster->blues / cluster->pixelCount);
       }
int distance(Cluster *cluster, int pixel) {
           int r = (pixel >> 16) & 0xff;
           int g = (pixel >> 8) & 0xff;
           int b = pixel & 0xff;
           int dr = abs(cluster->red - r);
           int dg = abs(cluster->green - g);
           int db = abs(cluster->blue - b);
           return (int)((dr + dg + db) / 3);
       }
int findMinimalCluster(Cluster clusters[], int amountClusters, int rgb) {
          int minimal = 1000000;
          int clusterNumber = 0;
           for (int i =0; i< amountClusters; i++)
                  {
                 int clusterDistance = distance(&clusters[i], rgb);
                 if (clusterDistance < minimal) {
                   minimal = clusterDistance;
                   clusterNumber = i;
                       }
                   }

        return clusterNumber;
}
 JNIEXPORT jobject JNICALL Java_com_cels_photofun_MainActivity_buildSegmented
   (JNIEnv *env, jobject obj, jobject bitmap, jint amountClusters){
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
    /*  Cluster cluster;
      cluster_with_id(&cluster, amountClusters);
      LOGD("Created cluster:%d", amountClusters);
      LOGD("Id %d", cluster.id);
      return NULL;*/
  //read pixels of bitmap into native memory :
  //
  //NOW tempPixels is correct array of image
 // uint32_t pixel = tempPixels[info.width * y + x]; // y = height
 //Cluster **clusters;
 //createClusters(clusters, amountClusters);
 Cluster clusters[20];
 LOGD("clusters created");
 for (int i =0; i< amountClusters; i++)
        {
            cluster_with_id(&clusters[i], i);
       }
  LOGD("each cluster created");
 //CLUSTERS ARE READY

    LOGD("reading bitmap pixels...");
     void* bitmapPixels;
     if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0)
       {
       LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
       return NULL;
       }
   uint32_t* src = (uint32_t*) bitmapPixels;

    uint32_t* tempPixels = (uint32_t*) malloc(sizeof(uint32_t)*info.height * info.width);
    int pixelsCount = info.height * info.width;
    memcpy(tempPixels, src, sizeof(uint32_t) * pixelsCount);
    AndroidBitmap_unlockPixels(env, bitmap);
        int currentX = 0;
        int currentY = 0;
        int differenceX = (int)(1*info.width / amountClusters);
        int differenceY = (int)(1*info.height / amountClusters);
      for (int i = 0; i< amountClusters; i++)
              {//1st color to clusters
                   uint32_t pixel = tempPixels[info.width *currentY + currentX];
                   addPixel(&clusters[i], (int)pixel);
                   LOGD("Id %d", clusters[i].id);
                   LOGD("Color_red %d", clusters[i].red);
                   currentX += differenceX;
                   currentY += differenceY;
             }
       //image is read, clusters are created
        int* idArray = (int*) malloc(sizeof(int)*info.height * info.width);
        for (int i = 0; i < info.height * info.width; i++)
        {
          idArray[i] = -1;
        }
        bool pixelChangedCluster = true;

                while (pixelChangedCluster == true) {
                    pixelChangedCluster = false;
                    for (int y = 0; y < info.height; y++) {
                        for (int x = 0; x < info.width; x++) {
                            int pixel = (int)tempPixels[info.width *y + x];
                            int clusterMinDistance = findMinimalCluster(clusters, amountClusters, pixel);
                            //Cluster cluster = findMinimalCluster(clusters, pixel);
                            if (idArray[x + info.width * y] != clusterMinDistance) {

                                if (idArray[x + info.width * y] != -1) {
                                    removePixel( &clusters[idArray[x + info.width * y]], pixel);
                                   // clusters[idArray[x + width * y]].removePixel(pixel);
                                }
                                addPixel( &clusters[clusterMinDistance], pixel);
                                //cluster.addPixel(pixel);
                                pixelChangedCluster = true;
                                idArray[x + info.width * y] = clusterMinDistance;
                            }
                        }
                    }

                }

 LOGD("Dude, everything is OK");
LOGD("creating new bitmap...");
 /* jclass bitmapCls = (*env)->GetObjectClass(env,bitmap);
 jmethodID createBitmapFunction = (*env)->GetStaticMethodID(env,bitmapCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
  jstring configName = (*env)->NewStringUTF(env,"ARGB_8888");
  jclass bitmapConfigClass = (*env)->FindClass(env,"android/graphics/Bitmap$Config");
  jmethodID valueOfBitmapConfigFunction = (*env)->GetStaticMethodID(env,bitmapConfigClass, "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
  jobject bitmapConfig = (*env)->CallStaticObjectMethod(env,bitmapConfigClass, valueOfBitmapConfigFunction, configName);
  jobject newBitmap = (*env)->CallStaticObjectMethod(env,bitmapCls, createBitmapFunction, info.width, info.height, bitmapConfig);

    uint32_t* newBitmapPixels = (uint32_t*) bitmapPixels;

     if ((ret = AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) < 0)
       {
       LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
       return NULL;
       }
*/

    for (int x = 0; x < info.width; x++)
      for (int y = 0; y < info.height; y++)
        {
        uint32_t pixel = tempPixels[info.width * y + x];
        src[info.width * y + x] = getRGB(&clusters[idArray[info.width * y + x]]);
        }
         for (int i = 0; i< amountClusters; i++)
         LOGE("Pixel_colour_red %d", getRGB(&clusters[i]));

//AndroidBitmap_unlockPixels(env, newBitmap);
//return newBitmap;
return NULL;
   }
