LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SDK_VERSION := current
LOCAL_MANIFEST_FILE := barcode-scanner/src/main/AndroidManifest.xml
LOCAL_SRC_FILES := \
    $(call all-java-files-under, barcode-scanner/src/main)
LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/barcode-scanner/src/main/res
    
LOCAL_MODULE := BarcodeScanner

include $(BUILD_STATIC_JAVA_LIBRARY)


include $(CLEAR_VARS)

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	/ndk/sources/android/support/include \
	$(LOCAL_PATH)/barcode-scanner/src/main/cpp/include \
	$(LOCAL_PATH)/barcode-scanner/src/main/cpp/jni \
	$(LOCAL_PATH)/barcode-scanner/src/main/cpp/zbar \
	$(LOCAL_PATH)/barcode-scanner/src/main/cpp/musl-locale

LOCAL_SRC_FILES := \
  barcode-scanner/src/main/cpp/jni/zbarjni.c \
	barcode-scanner/src/main/cpp/zbar/img_scanner.c \
	barcode-scanner/src/main/cpp/zbar/decoder.c \
	barcode-scanner/src/main/cpp/zbar/image.c \
	barcode-scanner/src/main/cpp/zbar/symbol.c \
	barcode-scanner/src/main/cpp/zbar/convert.c \
	barcode-scanner/src/main/cpp/zbar/config.c \
	barcode-scanner/src/main/cpp/zbar/scanner.c \
	barcode-scanner/src/main/cpp/zbar/error.c \
	barcode-scanner/src/main/cpp/zbar/refcnt.c \
	barcode-scanner/src/main/cpp/zbar/video.c \
	barcode-scanner/src/main/cpp/zbar/video/null.c \
	barcode-scanner/src/main/cpp/zbar/decoder/code128.c \
	barcode-scanner/src/main/cpp/zbar/decoder/code39.c \
	barcode-scanner/src/main/cpp/zbar/decoder/code93.c \
	barcode-scanner/src/main/cpp/zbar/decoder/codabar.c \
	barcode-scanner/src/main/cpp/zbar/decoder/databar.c \
	barcode-scanner/src/main/cpp/zbar/decoder/ean.c \
	barcode-scanner/src/main/cpp/zbar/decoder/i25.c \
	barcode-scanner/src/main/cpp/zbar/decoder/qr_finder.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/bch15_5.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/binarize.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/isaac.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/qrdec.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/qrdectxt.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/rs.c \
	barcode-scanner/src/main/cpp/zbar/qrcode/util.c \
	barcode-scanner/src/main/cpp/musl-locale/iconv.c

LOCAL_CFLAGS += -std=c99
LOCAL_MULTILIB := 32

LOCAL_MODULE := libzbarjni

include $(BUILD_SHARED_LIBRARY)
