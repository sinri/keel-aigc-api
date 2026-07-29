package io.github.sinri.keel.aigc.api.internal.agent.skill;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResource;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResourceContent;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResourceKind;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillScriptResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessCatholicSkillScriptExecutorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void executesScriptDirectlyAndReturnsBothOutputStreams() throws IOException {
        byte[] bytes = bytes("""
                #!/bin/sh
                printf 'out:%s' "$1"
                printf 'err:%s' "$2" >&2
                exit 7
                """);
        ProcessCatholicSkillScriptExecutor executor = executor(Duration.ofSeconds(5), 1024);

        CatholicSkillScriptResult result = await(executor.execute(
                "sample-skill", resource("scripts/run.sh", bytes),
                content("scripts/run.sh", bytes), List.of("hello", "problem")));

        assertEquals(7, result.exitCode());
        assertEquals("out:hello", result.stdout());
        assertEquals("err:problem", result.stderr());
        try (var children = Files.list(temporaryDirectory)) {
            assertTrue(children.findAny().isEmpty());
        }
    }

    @Test
    void limitsCapturedOutputWithoutBlockingTheProcess() {
        byte[] bytes = bytes("""
                #!/bin/sh
                printf '1234567890'
                printf 'abcdefghij' >&2
                """);
        ProcessCatholicSkillScriptExecutor executor = executor(Duration.ofSeconds(5), 5);

        CatholicSkillScriptResult result = await(executor.execute(
                "verbose-skill", resource("scripts/run.sh", bytes),
                content("scripts/run.sh", bytes), List.of()));

        assertEquals("12345\n...[truncated]", result.stdout());
        assertEquals("abcde\n...[truncated]", result.stderr());
    }

    @Test
    void terminatesScriptAfterTimeout() {
        byte[] bytes = bytes("""
                #!/bin/sh
                while :; do :; done
                """);
        ProcessCatholicSkillScriptExecutor executor = executor(Duration.ofMillis(100), 1024);

        CompletionException exception = assertThrows(CompletionException.class, () -> await(executor.execute(
                "slow-skill", resource("scripts/run.sh", bytes),
                content("scripts/run.sh", bytes), List.of())));

        assertTrue(exception.getCause().getMessage().contains("timed out"));
    }

    private ProcessCatholicSkillScriptExecutor executor(Duration timeout, int maxOutputBytes) {
        return new ProcessCatholicSkillScriptExecutor(temporaryDirectory, timeout, maxOutputBytes);
    }

    private static CatholicSkillResource resource(String path, byte[] bytes) {
        return new CatholicSkillResource(path, CatholicSkillResourceKind.SCRIPT, bytes.length, "text/x-shellscript");
    }

    private static CatholicSkillResourceContent content(String path, byte[] bytes) {
        return new CatholicSkillResourceContent(path, "text/x-shellscript", bytes);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static <T> T await(io.vertx.core.Future<T> future) {
        return future.toCompletionStage().toCompletableFuture().join();
    }
}
