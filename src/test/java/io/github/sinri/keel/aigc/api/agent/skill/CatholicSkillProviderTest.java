package io.github.sinri.keel.aigc.api.agent.skill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicSkillProviderTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void localProviderDiscoversFrontmatterInStableOrder() throws IOException {
        writeSkill("z-last", """
            ---
            name: z-last
            description: Last skill
            ---
            # Last
            """);
        writeSkill("a-first", """
            ---
            name: a-first
            description: First skill
            license: Apache-2.0
            compatibility: Requires Java 17
            metadata:
              author: keel
              version: "1"
            allowed-tools: Read Write
            ---
            # First
            These instructions must only be loaded on activation.
            """);
        Files.createDirectory(temporaryDirectory.resolve("without-skill-file"));
        Files.writeString(temporaryDirectory.resolve("README.md"), "ignored");

        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);
        List<CatholicSkillFrontmatter> candidates = await(provider.getSkillCandidates());

        assertEquals(List.of("a-first", "z-last"), candidates.stream().map(CatholicSkillFrontmatter::name).toList());
        CatholicSkillFrontmatter first = candidates.get(0);
        assertEquals("First skill", first.description());
        assertEquals("Apache-2.0", first.license());
        assertEquals("Requires Java 17", first.compatibility());
        assertEquals("keel", first.metadata().get("author"));
        assertEquals("1", first.metadata().get("version"));
        assertEquals("Read Write", first.allowedTools());
        assertThrows(UnsupportedOperationException.class, () -> first.metadata().put("x", "y"));
    }

    @Test
    void localProviderLoadsCompleteSkillOnDemand() throws IOException {
        writeSkill("code-review", """
            ---
            name: code-review
            description: Review source code
            ---

            # Review instructions

            Inspect the code carefully.
            """);
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        CatholicSkill skill = await(provider.loadSkillByName("code-review"));

        assertEquals("code-review", skill.name());
        assertEquals("Review source code", skill.description());
        assertEquals("# Review instructions\n\nInspect the code carefully.", skill.instructions());
        assertEquals(temporaryDirectory.resolve("code-review").toAbsolutePath().normalize(),
                skill.directory());
        assertTrue(skill.metadata().isEmpty());
    }

    @Test
    void localProviderDiscoversAndReadsResourcesOnDemand() throws IOException {
        writeSkill("document-processing", """
            ---
            name: document-processing
            description: Process documents
            ---
            Read references/format.md and use assets/header.bin.
            """);
        Path root = temporaryDirectory.resolve("document-processing");
        Files.createDirectories(root.resolve("references"));
        Files.writeString(root.resolve("references/format.md"), "# Format\nUse UTF-8.");
        Files.createDirectories(root.resolve("scripts"));
        Files.writeString(root.resolve("scripts/convert.py"), "print('ok')");
        Files.createDirectories(root.resolve("assets"));
        Files.write(root.resolve("assets/header.bin"), new byte[]{0, 1, 2});

        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);
        CatholicSkill skill = await(provider.loadSkillByName("document-processing"));

        assertEquals(List.of("assets/header.bin", "references/format.md", "scripts/convert.py"),
                skill.resources().stream().map(CatholicSkillResource::path).toList());
        assertEquals(CatholicSkillResourceKind.ASSET, skill.resources().get(0).kind());
        assertEquals(CatholicSkillResourceKind.REFERENCE, skill.resources().get(1).kind());
        assertEquals(CatholicSkillResourceKind.SCRIPT, skill.resources().get(2).kind());
        CatholicSkillResourceContent content =
                await(provider.readSkillResource("document-processing", "references/format.md"));
        assertEquals("# Format\nUse UTF-8.", new String(content.bytes(), StandardCharsets.UTF_8));
    }

    @Test
    void localProviderRejectsResourcePathTraversalAndSkillFileAccess() throws IOException {
        writeSkill("safe-skill", """
            ---
            name: safe-skill
            description: Safe skill
            ---
            Instructions
            """);
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        assertInstanceOf(IllegalArgumentException.class,
                failureOf(provider.readSkillResource("safe-skill", "../outside.txt")));
        assertInstanceOf(IllegalArgumentException.class,
                failureOf(provider.readSkillResource("safe-skill", "SKILL.md")));
    }

    @Test
    void localProviderDoesNotDiscloseOrReadSymlinks() throws IOException {
        writeSkill("safe-skill", """
            ---
            name: safe-skill
            description: Safe skill
            ---
            Instructions
            """);
        Path outside = temporaryDirectory.resolve("secret.txt");
        Files.writeString(outside, "secret");
        Path link = temporaryDirectory.resolve("safe-skill/references/secret.txt");
        Files.createDirectories(link.getParent());
        Files.createSymbolicLink(link, outside);
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        assertTrue(await(provider.loadSkillByName("safe-skill")).resources().isEmpty());
        assertInstanceOf(IllegalArgumentException.class,
                failureOf(provider.readSkillResource("safe-skill", "references/secret.txt")));
    }

    @Test
    void localProviderReturnsEmptyCandidatesWhenRootDoesNotExist() {
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(
            temporaryDirectory.resolve("not-created"));

        assertTrue(await(provider.getSkillCandidates()).isEmpty());
    }

    @Test
    void localProviderRejectsDirectoryAndFrontmatterNameMismatch() throws IOException {
        writeSkill("directory-name", """
            ---
            name: another-name
            description: Invalid skill
            ---
            Instructions
            """);
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        CompletionException exception = assertThrows(CompletionException.class,
            () -> await(provider.getSkillCandidates()));

        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(cause.getMessage().contains("does not match directory"));
    }

    @Test
    void localProviderRejectsMalformedFrontmatter() throws IOException {
        writeSkill("broken", """
            ---
            name: broken
            description: [not, a, string]
            ---
            Instructions
            """);
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        CompletionException exception = assertThrows(CompletionException.class,
            () -> await(provider.getSkillCandidates()));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void localProviderRejectsUnsafeSkillName() {
        CatholicSkillProvider provider = CatholicSkillProvider.createLocalSkillProvider(temporaryDirectory);

        CompletionException exception = assertThrows(CompletionException.class,
            () -> await(provider.loadSkillByName("../outside")));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    private void writeSkill(String directoryName, String content) throws IOException {
        Path directory = Files.createDirectories(temporaryDirectory.resolve(directoryName));
        Files.writeString(directory.resolve("SKILL.md"), content);
    }

    private static <T> T await(io.vertx.core.Future<T> future) {
        return future.toCompletionStage().toCompletableFuture().join();
    }

    private static Throwable failureOf(io.vertx.core.Future<?> future) {
        CompletionException exception = assertThrows(CompletionException.class,
                () -> future.toCompletionStage().toCompletableFuture().join());
        return exception.getCause();
    }
}
