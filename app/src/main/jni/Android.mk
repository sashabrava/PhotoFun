LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := buildSegmented
LOCAL_LDFLAGS := -Wl,--build-id,-ljnigraphics
LOCAL_SRC_FILES := \
	D:\java\PhotoFun\app\src\main\jni\buildSegmented.c \
LOCAL_SHARED_LIBRARIES := libutils libcutils

LOCAL_LDLIBS := -llog 
LOCAL_C_INCLUDES += D:\java\PhotoFun\app\src\main\jni
LOCAL_C_INCLUDES += D:\java\PhotoFun\app\src\debug\jni
LOCAL_CFLAGS := -g
include $(BUILD_SHARED_LIBRARY)
