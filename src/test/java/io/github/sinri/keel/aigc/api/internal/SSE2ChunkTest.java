package io.github.sinri.keel.aigc.api.internal;

import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SSE2ChunkTest {

    @Test
    void buildsResponseAfterSuccessfulStream() {
        Future<String> result = SSE2Chunk.buildResponseOnSuccess(
                Future.succeededFuture(),
                () -> "complete response"
        );

        assertTrue(result.succeeded());
        assertEquals("complete response", result.result());
    }

    @Test
    void preservesOriginalStreamFailureWithoutBuildingPartialResponse() {
        RuntimeException originalFailure = new RuntimeException("stream interrupted");
        AtomicBoolean responseBuilderCalled = new AtomicBoolean(false);

        Future<String> result = SSE2Chunk.buildResponseOnSuccess(
                Future.failedFuture(originalFailure),
                () -> {
                    responseBuilderCalled.set(true);
                    return "partial response";
                }
        );

        assertTrue(result.failed());
        assertSame(originalFailure, result.cause());
        assertFalse(responseBuilderCalled.get());
    }
}
