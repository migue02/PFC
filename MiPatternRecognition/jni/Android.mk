LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := nonfree
LOCAL_SRC_FILES := libnonfree.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := opencv_java_prebuilt
LOCAL_SRC_FILES := libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
include C:\NVPACK\OpenCV-2.4.5-Tegra-sdk-r2\sdk\native\jni\OpenCV.mk
OPENCV_CAMERA_MODULES:=off
LOCAL_MODULE    := mipattern_recognition
LOCAL_SHARED_LIBRARIES := nonfree opencv_java_prebuilt
LOCAL_SRC_FILES := PatRecModificado2.cpp
LOCAL_LDLIBS +=  -llog -ldl
include $(BUILD_SHARED_LIBRARY)

