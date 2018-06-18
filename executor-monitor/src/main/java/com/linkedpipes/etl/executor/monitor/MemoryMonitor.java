package com.linkedpipes.etl.executor.monitor;

import org.slf4j.Logger;

public class MemoryMonitor {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final long MB = 1024 * 1024;

    private MemoryMonitor() {

    }

    public static void log(Logger LOG , String message) {
        long allocated = RUNTIME.totalMemory();
        long free = RUNTIME.freeMemory();
        long available = RUNTIME.maxMemory();
        long used = allocated - free;
        LOG.info("{} : {} / {}",
                message, used / MB, available / MB);
    }

}
