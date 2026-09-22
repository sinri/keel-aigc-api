package io.github.sinri.keel.aigc.api.trace;

import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;

/** Private JSON files with atomic publication and bounded retention. Use a dedicated directory. */
public final class CatholicFileTraceSink implements CatholicTraceSink {
    private final Path directory;
    private final long maxBytes;
    private final int maxFiles;
    private final Duration retention;

    public CatholicFileTraceSink(Path directory) { this(directory, 1L << 30, 10000, Duration.ofDays(7)); }
    public CatholicFileTraceSink(Path directory, long maxBytes, int maxFiles, Duration retention) {
        this.directory = directory.toAbsolutePath().normalize();
        if (maxBytes < 1 || maxFiles < 1 || retention.isNegative() || retention.isZero())
            throw new IllegalArgumentException("invalid retention");
        this.maxBytes = maxBytes;
        this.maxFiles = maxFiles;
        this.retention = retention;
    }

    @Override public synchronized String save(JsonObject trace) throws IOException {
        String id = UUID.fromString(trace.getString("trace_id")).toString();
        Files.createDirectories(directory);
        if (Files.isSymbolicLink(directory)) throw new IOException("trace directory cannot be a symbolic link");
        try { Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------")); }
        catch (UnsupportedOperationException ignored) { /* Storage ACLs are the deployment's responsibility. */ }
        byte[] data = trace.encode().getBytes(StandardCharsets.UTF_8);
        if (data.length > maxBytes) throw new IOException("trace exceeds disk budget");
        prune(data.length);
        Path pending = Files.createTempFile(directory, ".trace-", ".tmp");
        try {
            try { Files.setPosixFilePermissions(pending, PosixFilePermissions.fromString("rw-------")); }
            catch (UnsupportedOperationException ignored) { }
            Files.write(pending, data);
            Path target = directory.resolve("trace-" + id + ".json");
            Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE);
            return target.toString();
        } finally { Files.deleteIfExists(pending); }
    }

    @Override public synchronized void maintenance() throws IOException {
        if (Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) prune(0);
    }

    private void prune(long incoming) throws IOException {
        long cutoff = System.currentTimeMillis() - retention.toMillis();
        try (var paths = Files.list(directory)) {
            var files = paths.filter(p -> p.getFileName().toString().matches("trace-[0-9a-f-]{36}\\.json"))
                    .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                    .sorted(Comparator.comparingLong(p -> {
                        try { return Files.getLastModifiedTime(p).toMillis(); }
                        catch (IOException e) { return Long.MAX_VALUE; }
                    })).toList();
            long total = 0;
            for (Path file : files) total += Files.size(file);
            int count = files.size();
            for (Path file : files) {
                if (Files.getLastModifiedTime(file).toMillis() >= cutoff && total + incoming <= maxBytes && count <= maxFiles - (incoming > 0 ? 1 : 0)) continue;
                long size = Files.size(file);
                Files.delete(file);
                total -= size;
                count--;
            }
        }
    }
}
