package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCallImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicToolInvocationObserverTest {
    private static final CatholicFunctionToolCall TOOL_CALL = new CatholicFunctionToolCallImpl(
        "call-1", new FunctionCall("weather", "{\"city\":\"Hangzhou\"}")
    );

    @Test
    void observesSuccessfulInvocation() {
        RecordingObserver observer = new RecordingObserver();
        CatholicToolInvocationHandler handler = CatholicToolInvocationHandler.of(
            call -> Future.succeededFuture("{\"temperature\":30}"));

        CatholicToolInvocationHandler observed = handler.observedBy(observer);
        String result = observed.handle(TOOL_CALL).toCompletionStage().toCompletableFuture().join();

        assertSame(handler, observed);
        assertEquals("{\"temperature\":30}", result);
        assertEquals(List.of("started", "succeeded"), observer.events);
        assertEquals("call-1", observer.observation.toolCallId());
        assertEquals("weather", observer.observation.functionName());
        assertEquals("{\"city\":\"Hangzhou\"}", observer.observation.arguments());
        assertNotNull(observer.observation.invocationId());
        assertEquals(result, observer.result);
        assertTrue(observer.elapsedMillis >= 0);
    }

    @Test
    void observesAsynchronousAndSynchronousFailures() {
        RuntimeException asynchronous = new RuntimeException("async");
        RecordingObserver asyncObserver = new RecordingObserver();
        CatholicToolInvocationHandler asyncHandler = CatholicToolInvocationHandler.observed(
            CatholicToolInvocationHandler.of(call -> Future.failedFuture(asynchronous)), asyncObserver
        );
        assertSame(asynchronous, failureOf(asyncHandler.handle(TOOL_CALL)));
        assertEquals(List.of("started", "failed"), asyncObserver.events);
        assertSame(asynchronous, asyncObserver.failure);

        RuntimeException synchronous = new RuntimeException("sync");
        RecordingObserver syncObserver = new RecordingObserver();
        CatholicToolInvocationHandler syncHandler = CatholicToolInvocationHandler.observed(
            CatholicToolInvocationHandler.of(call -> {
                throw synchronous;
            }), syncObserver
        );
        assertSame(synchronous, failureOf(syncHandler.handle(TOOL_CALL)));
        assertEquals(List.of("started", "failed"), syncObserver.events);
        assertSame(synchronous, syncObserver.failure);
    }

    @Test
    void observerFailureDoesNotAlterToolResult() {
        CatholicToolInvocationObserver broken = new CatholicToolInvocationObserver() {
            @Override
            public void onStarted(CatholicToolInvocationObservation observation) {
                throw new IllegalStateException("audit sink unavailable");
            }

            @Override
            public void onSucceeded(
                CatholicToolInvocationObservation observation, String result, long elapsedMillis
            ) {
                throw new IllegalStateException("audit sink unavailable");
            }
        };
        CatholicToolInvocationHandler handler = CatholicToolInvocationHandler.observed(
            CatholicToolInvocationHandler.of(call -> Future.succeededFuture("ok")), broken
        );

        assertEquals("ok", handler.handle(TOOL_CALL).toCompletionStage().toCompletableFuture().join());
    }

    @Test
    void nativeAdapterFactoryObservesRoutingFailure() {
        RecordingObserver observer = new RecordingObserver();
        CatholicToolInvocationHandler handler =
            CatholicToolInvocationHandler.createWithNativeFunctionAdapters(observer);

        Throwable failure = failureOf(handler.handle(TOOL_CALL));

        assertTrue(failure.getMessage().contains("weather"));
        assertEquals(List.of("started", "failed"), observer.events);
    }

    private static Throwable failureOf(Future<?> future) {
        Exception exception = assertThrows(Exception.class,
            () -> future.toCompletionStage().toCompletableFuture().join());
        return exception.getCause() == null ? exception : exception.getCause();
    }

    private static final class RecordingObserver implements CatholicToolInvocationObserver {
        private final List<String> events = new ArrayList<>();
        private CatholicToolInvocationObservation observation;
        private String result;
        private Throwable failure;
        private long elapsedMillis;

        @Override
        public void onStarted(CatholicToolInvocationObservation observation) {
            events.add("started");
            this.observation = observation;
        }

        @Override
        public void onSucceeded(
            CatholicToolInvocationObservation observation, String result, long elapsedMillis
        ) {
            events.add("succeeded");
            this.result = result;
            this.elapsedMillis = elapsedMillis;
        }

        @Override
        public void onFailed(
            CatholicToolInvocationObservation observation, Throwable cause, long elapsedMillis
        ) {
            events.add("failed");
            this.failure = cause;
            this.elapsedMillis = elapsedMillis;
        }
    }
}
