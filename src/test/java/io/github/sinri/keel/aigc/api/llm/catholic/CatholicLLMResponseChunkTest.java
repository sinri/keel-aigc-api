package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicLLMResponseChunkTest {

    @Test
    void buildsTextlessToolCallChunk() {
        CatholicToolCallChunkDelta toolDelta = new CatholicToolCallChunkDelta(
                "call_1",
                "function",
                0,
                new CatholicToolCallChunkDelta.CatholicToolCallFunctionChunkDelta(null, "{\"x\":")
        );

        CatholicLLMResponseChunk chunk = assertDoesNotThrow(() -> CatholicLLMResponseChunk.builder()
                .id("response_1")
                .deltaToolCalls(List.of(toolDelta))
                .build());

        assertNull(chunk.deltaText());
        assertTrue(chunk.hasDeltaToolCalls());
    }

    @Test
    void buildsTextlessFinishedChunk() {
        CatholicLLMResponseChunk chunk = assertDoesNotThrow(() -> CatholicLLMResponseChunk.builder()
                .id("response_1")
                .markFinished()
                .build());

        assertTrue(chunk.isFinished());
        assertNull(chunk.deltaText());
        assertFalse(chunk.hasDeltaToolCalls());
        assertEquals(0, chunk.deltaToolCalls().size());
    }

    @Test
    void rejectsChunkWithoutResponseId() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CatholicLLMResponseChunk.builder().markFinished().build()
        );

        assertEquals("CatholicLLMResponseChunk id must be assigned", exception.getMessage());
    }
}
