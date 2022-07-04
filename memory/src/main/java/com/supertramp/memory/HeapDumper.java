package com.supertramp.memory;

import android.os.Debug;

import java.io.IOException;

public class HeapDumper {

    static {
        System.loadLibrary("xhook");
        System.loadLibrary("kwai-android-base");
        System.loadLibrary("heapdump");
    }

    /**
     * 堆转储
     * @param strip 是否裁剪hprof文件
     */
    public static void dump(String path, boolean strip) {
        try {
            if (strip) {
                stripDump(path);
            }
            else {
                Debug.dumpHprofData(path);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 快照裁剪
     * @throws IOException
     */
    private static void stripDump(String path) throws IOException {
        initStripDump();
        hprofName(path);
        Debug.dumpHprofData(path);
    }

    /**
     * 在子进程中dump堆内存
     */
    public static boolean forkAndDump(String path, boolean strip) {
        nativeInit();
        int pid = suspendAndFork();
        if (pid == 0) {
            dump(path, strip);
            exitProcess();
            return false;
        }
        else {
            return resumeAndWait(pid);
        }
    }

    public native static void initStripDump();

    public native static void hprofName(String name);

    public native static void nativeInit();

    /**
     * fork前挂起所有线程，骗过虚拟机（因为虚拟机dump前需要挂起所有线程 stop the world）
     * @return 返回子进程id
     */
    private native static int suspendAndFork();

    private native static boolean resumeAndWait(int pid);

    private native static void exitProcess();

}
