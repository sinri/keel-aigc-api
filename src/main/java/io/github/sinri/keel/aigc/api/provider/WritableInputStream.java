package io.github.sinri.keel.aigc.api.provider;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@TechnicalPreview
public class WritableInputStream extends InputStream {
    private final Queue<Byte> byteQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean ended = false;
    private final Object lock = new Object();

    public WritableInputStream() {

    }

    public void accept(Buffer buffer) {
        synchronized (lock) {
            for (byte b : buffer.getBytes()) {
                byteQueue.add(b);
            }
            lock.notifyAll();
        }
    }

    public void end() {
        synchronized (lock) {
            ended = true;
            lock.notifyAll();
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (lock) {
            while (byteQueue.isEmpty() && !ended) {
                try {
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted", e);
                }
            }
        }

        Byte headByte = byteQueue.poll();
        if (headByte == null) {
            return -1;
        }
        return headByte & 0xFF;
    }
}
