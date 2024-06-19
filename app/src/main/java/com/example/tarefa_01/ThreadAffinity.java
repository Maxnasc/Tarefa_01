package com.example.tarefa_01;

public class ThreadAffinity {
    static {
        System.loadLibrary("ThreadAffinity");
    }

    public native void setThreadAffinity(int cpuId);
}
