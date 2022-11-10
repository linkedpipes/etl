package com.linkedpipes.etl.storage.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

@Service
public class MemoryMonitor {

    private static final Logger LOG =
            LoggerFactory.getLogger(MemoryMonitor.class);

    private long lastUsedMb = 0;

    @Scheduled(fixedDelay = 15 * 1000)
    public void log() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage();
        long usedMb = heap.getUsed() / (1024 * 1024);
        long committedMb = heap.getCommitted() / (1024 * 1024);
        if (lastUsedMb - usedMb < 8) {
            return;
        }
        lastUsedMb = usedMb;
        LOG.debug("Memory used {} MB committed {} MB", usedMb, committedMb);
    }

}
