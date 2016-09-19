package com.linkedpipes.plugin.extractor.ftpfiles;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Petr Škoda
 */
class ProgressPrinter implements CopyStreamListener {

    private static final Logger LOG
            = LoggerFactory.getLogger(ProgressPrinter.class);

    long lastDownloaded = 0;

    @Override
    public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(),
                event.getBytesTransferred(), event.getStreamSize());
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred,
            int bytesTransferred, long streamSize) {
        if (totalBytesTransferred > lastDownloaded) {
            lastDownloaded += (1024 * 1024);
            LOG.debug("Transferred: {} MB, {} B",
                    totalBytesTransferred / (1024 * 1024),
                    totalBytesTransferred);
        }
    }

}
