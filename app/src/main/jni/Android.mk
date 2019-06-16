LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := buildSegmented
LOCAL_LDFLAGS := -Wl,--build-id,-ljnigraphics
LOCAL_SRC_FILES :=  $(LOCAL_PATH)\buildSegmented.c
LOCAL_LDLIBS += -lcutils -lutils

LOCAL_LDLIBS := -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_CFLAGS := -g
include $(BUILD_SHARED_LIBRARY)
