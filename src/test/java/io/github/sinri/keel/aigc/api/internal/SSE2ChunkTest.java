package io.github.sinri.keel.aigc.api.internal;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;

import java.util.ArrayList;
import java.util.List;
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
    @Test
    void dependencyFramesAllLineEndingsAcrossEveryBufferSplit() {
        for (String ending : List.of("\n", "\r\n", "\r")) {
            Buffer input = Buffer.buffer("data: first" + ending + ending
                + "event: result" + ending + "data: 中文🙂" + ending + ending);
            List<String> expected = List.of("data: first", "event: result\ndata: 中文🙂");
            for (int split = 0; split <= input.length(); split++) {
                var cutter = new FramingProbe();
                var events = new ArrayList<>(cutter.feed(input.getBuffer(0, split)));
                events.addAll(cutter.feed(input.getBuffer(split, input.length())));
                assertEquals(expected, events, "split=" + split + ", ending=" + ending);
            }
            var cutter = new FramingProbe();
            var events = new ArrayList<String>();
            for (int i = 0; i < input.length(); i++) {
                events.addAll(cutter.feed(input.getBuffer(i, i + 1)));
            }
            assertEquals(expected, events);
        }
    }

    @Test
    void dependencyPreservesIncompleteEventBytes() {
        var cutter = new FramingProbe();
        Buffer input = Buffer.buffer("data: complete\r\n\r\ndata: 中文");
        assertEquals(List.of("data: complete"), cutter.feed(input));
        assertEquals(List.of(), cutter.feed(Buffer.buffer()));
        assertEquals("data: 中文", cutter.pending().toString());
        assertEquals(List.of("data: 中文"), cutter.feed(Buffer.buffer("\r\r")));
    }

    private static class FramingProbe extends IntravenouslyCutterOnString {
        FramingProbe() {
            super(event -> Future.succeededFuture());
        }

        List<String> feed(Buffer buffer) {
            getBufferRef().get().appendBuffer(buffer);
            return cut();
        }

        Buffer pending() {
            return getBufferRef().get();
        }
    }

}
