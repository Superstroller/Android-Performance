#include <jni.h>
#include "xhook.h"
#include "hprof_strip.h"
#include "hprof_dump.h"

using namespace kwai::leak_monitor;

extern "C"
JNIEXPORT void JNICALL
Java_com_supertramp_heapdump_HeapDumper_initStripDump(JNIEnv *env, jclass clazz) {
    HprofStrip::HookInit();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_supertramp_heapdump_HeapDumper_hprofName(JNIEnv *env, jclass clazz, jstring name) {
    const char *hprofName = env->GetStringUTFChars(name, nullptr);
    HprofStrip::GetInstance().SetHprofName(hprofName);
    env->ReleaseStringUTFChars(name, hprofName);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_supertramp_heapdump_HeapDumper_nativeInit(JNIEnv *env, jclass clazz) {
    HprofDump::GetInstance().Initialize();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_supertramp_heapdump_HeapDumper_suspendAndFork(JNIEnv *env, jclass clazz) {
    return HprofDump::GetInstance().SuspendAndFork();
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_supertramp_heapdump_HeapDumper_resumeAndWait(JNIEnv *env, jclass clazz, jint pid) {
    return HprofDump::GetInstance().ResumeAndWait(pid);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_supertramp_heapdump_HeapDumper_exitProcess(JNIEnv *env, jclass clazz) {
    _exit(0);
}