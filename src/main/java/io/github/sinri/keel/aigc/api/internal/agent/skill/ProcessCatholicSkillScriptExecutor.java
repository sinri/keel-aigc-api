package io.github.sinri.keel.aigc.api.internal.agent.skill;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResource;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResourceContent;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillResourceKind;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillScriptExecutor;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillScriptResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 将脚本写入隔离的临时目录，并通过新进程直接执行。
 *
 * <p>脚本需要可被操作系统直接执行；在类 Unix 系统上通常应包含正确的 shebang。
 * 该实现不会通过 shell 拼接命令，因此参数会原样传递给脚本进程。</p>
 */
public final class ProcessCatholicSkillScriptExecutor implements CatholicSkillScriptExecutor {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    public static final int DEFAULT_MAX_OUTPUT_BYTES = 1024 * 1024;

    private static final String TRUNCATED_MARKER = "\n...[truncated]";
    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger();
    private static final ExecutorService PROCESS_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable,
                "keel-skill-script-" + THREAD_SEQUENCE.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    private final Path temporaryDirectory;
    private final Duration timeout;
    private final int maxOutputBytes;

    public ProcessCatholicSkillScriptExecutor() {
        this(Path.of(System.getProperty("java.io.tmpdir"), "keel-skill-scripts"),
                DEFAULT_TIMEOUT, DEFAULT_MAX_OUTPUT_BYTES);
    }

    public ProcessCatholicSkillScriptExecutor(Duration timeout, int maxOutputBytes) {
        this(Path.of(System.getProperty("java.io.tmpdir"), "keel-skill-scripts"),
                timeout, maxOutputBytes);
    }

    public ProcessCatholicSkillScriptExecutor(Path temporaryDirectory, Duration timeout, int maxOutputBytes) {
        this.temporaryDirectory = Objects.requireNonNull(temporaryDirectory, "temporaryDirectory")
                .toAbsolutePath().normalize();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (maxOutputBytes < 1) {
            throw new IllegalArgumentException("maxOutputBytes must be at least 1");
        }
        this.maxOutputBytes = maxOutputBytes;
    }

    @Override
    public Future<CatholicSkillScriptResult> execute(
            String skillName,
            CatholicSkillResource script,
            CatholicSkillResourceContent content,
            List<String> arguments
    ) {
        Objects.requireNonNull(skillName, "skillName");
        Objects.requireNonNull(script, "script");
        Objects.requireNonNull(content, "content");
        List<String> copiedArguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        return asynchronously(() -> executeBlocking(skillName, script, content, copiedArguments));
    }

    private CatholicSkillScriptResult executeBlocking(
            String skillName,
            CatholicSkillResource script,
            CatholicSkillResourceContent content,
            List<String> arguments
    ) {
        validateScript(script, content);

        Path workspace = null;
        try {
            Files.createDirectories(temporaryDirectory);
            workspace = Files.createTempDirectory(temporaryDirectory, temporaryPrefix(skillName));
            Path scriptPath = workspace.resolve(script.path()).normalize();
            if (!scriptPath.startsWith(workspace)) {
                throw new IllegalArgumentException("script path escapes temporary workspace: " + script.path());
            }
            Path parent = scriptPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(scriptPath, content.bytes());
            makeExecutable(scriptPath);

            List<String> command = new ArrayList<>(arguments.size() + 1);
            command.add(scriptPath.toString());
            command.addAll(arguments);
            return runProcess(skillName, workspace, command);
        } catch (IOException e) {
            throw new IllegalStateException("failed to execute skill script: " + script.path(), e);
        } finally {
            deleteRecursively(workspace);
        }
    }

    private CatholicSkillScriptResult runProcess(String skillName, Path workspace, List<String> command)
            throws IOException {
        Process process = new ProcessBuilder(command)
                .directory(workspace.toFile())
                .start();
        process.getOutputStream().close();

        CompletableFuture<CapturedOutput> stdout = capture(process.getInputStream());
        CompletableFuture<CapturedOutput> stderr = capture(process.getErrorStream());
        boolean completed;
        try {
            completed = process.waitFor(Math.max(1L, timeout.toMillis()), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            terminate(process);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while executing skill script: " + skillName, e);
        }
        if (!completed) {
            terminate(process);
            awaitCapturedOutput(stdout);
            awaitCapturedOutput(stderr);
            throw new IllegalStateException(
                    "skill script timed out after " + timeout.toMillis() + " ms: " + skillName);
        }

        CapturedOutput capturedStdout = awaitCapturedOutput(stdout);
        CapturedOutput capturedStderr = awaitCapturedOutput(stderr);
        return new CatholicSkillScriptResult(
                process.exitValue(), capturedStdout.asString(), capturedStderr.asString());
    }

    private CompletableFuture<CapturedOutput> capture(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] retained = new byte[maxOutputBytes];
            byte[] buffer = new byte[8192];
            int retainedBytes = 0;
            boolean truncated = false;
            try (inputStream) {
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    int copied = Math.min(read, maxOutputBytes - retainedBytes);
                    if (copied > 0) {
                        System.arraycopy(buffer, 0, retained, retainedBytes, copied);
                        retainedBytes += copied;
                    }
                    if (copied < read) truncated = true;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new CapturedOutput(retained, retainedBytes, truncated);
        }, PROCESS_EXECUTOR);
    }

    private static CapturedOutput awaitCapturedOutput(CompletableFuture<CapturedOutput> capturedOutput) {
        return capturedOutput.join();
    }

    private static void validateScript(
            CatholicSkillResource script,
            CatholicSkillResourceContent content
    ) {
        if (script.kind() != CatholicSkillResourceKind.SCRIPT) {
            throw new IllegalArgumentException("resource is not a script: " + script.path());
        }
        if (!script.path().equals(content.path())) {
            throw new IllegalArgumentException("script resource and content paths do not match");
        }
        if (script.size() != content.bytes().length) {
            throw new IllegalArgumentException("script resource and content sizes do not match");
        }
    }

    private static String temporaryPrefix(String skillName) {
        String prefix = skillName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (prefix.isBlank()) prefix = "skill";
        if (prefix.length() > 48) prefix = prefix.substring(0, 48);
        return prefix + "-";
    }

    private static void makeExecutable(Path scriptPath) throws IOException {
        try {
            Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
            permissions.addAll(Files.getPosixFilePermissions(scriptPath));
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(scriptPath, permissions);
        } catch (UnsupportedOperationException ignored) {
            if (!scriptPath.toFile().setExecutable(true, true)) {
                throw new IOException("failed to make script executable: " + scriptPath);
            }
        }
    }

    private static void terminate(Process process) {
        List<ProcessHandle> descendants = process.descendants().toList();
        descendants.forEach(ProcessHandle::destroy);
        process.destroy();
        try {
            if (!process.waitFor(200, TimeUnit.MILLISECONDS)) {
                descendants.forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (InterruptedException e) {
            descendants.forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    private static void deleteRecursively(Path root) {
        if (root == null) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 临时目录的清理失败不覆盖脚本执行结果。
                }
            });
        } catch (IOException ignored) {
            // 临时目录的清理失败不覆盖脚本执行结果。
        }
    }

    private static <T> Future<T> asynchronously(Supplier<T> supplier) {
        Promise<T> promise = Promise.promise();
        PROCESS_EXECUTOR.execute(() -> {
            try {
                promise.complete(supplier.get());
            } catch (Throwable throwable) {
                promise.fail(throwable);
            }
        });
        return promise.future();
    }

    private record CapturedOutput(byte[] bytes, int length, boolean truncated) {
        private String asString() {
            String value = new String(bytes, 0, length, StandardCharsets.UTF_8);
            return truncated ? value + TRUNCATED_MARKER : value;
        }
    }
}
