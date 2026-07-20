package io.github.sinri.keel.aigc.api.agent.skill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionException;

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
        assertTrue(skill.metadata().isEmpty());
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
}
