# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
cmake_minimum_required(VERSION 3.4.1)

set( cpp-dir src/main/cpp )
set( jni-dir ${cpp-dir}/jni )
set( zbar-dir ${cpp-dir}/zbar )
set( musl-locale-dir ${cpp-dir}/musl-locale )
set( support-dir ${ANDROID_NDK}/sources/android/support )

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
add_library(
	# Specifies the name of the library.
	zbarjni

	# Sets the library as a shared library.
	SHARED

	# Provides a relative path to your source file(s).
	${jni-dir}/zbarjni.c
    ${musl-locale-dir}/iconv.c
	${zbar-dir}/img_scanner.c
	${zbar-dir}/decoder.c
	${zbar-dir}/image.c
	${zbar-dir}/symbol.c
	${zbar-dir}/convert.c
	${zbar-dir}/config.c
	${zbar-dir}/scanner.c
	${zbar-dir}/error.c
	${zbar-dir}/refcnt.c
	${zbar-dir}/video.c
	${zbar-dir}/video/null.c
	${zbar-dir}/decoder/code128.c
	${zbar-dir}/decoder/code39.c
	${zbar-dir}/decoder/code93.c
	${zbar-dir}/decoder/codabar.c
	${zbar-dir}/decoder/databar.c
	${zbar-dir}/decoder/ean.c
	${zbar-dir}/decoder/i25.c
	${zbar-dir}/decoder/qr_finder.c
	${zbar-dir}/qrcode/bch15_5.c
	${zbar-dir}/qrcode/binarize.c
	${zbar-dir}/qrcode/isaac.c
	${zbar-dir}/qrcode/qrdec.c
	${zbar-dir}/qrcode/qrdectxt.c
	${zbar-dir}/qrcode/rs.c
	${zbar-dir}/qrcode/util.c )

include_directories( ${cpp-dir}/include ${jni-dir} ${zbar-dir} )
