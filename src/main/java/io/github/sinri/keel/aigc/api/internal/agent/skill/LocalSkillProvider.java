package io.github.sinri.keel.aigc.api.internal.agent.skill;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillProvider;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkill;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillFrontmatter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * 基于本地文件系统，提供 Skill 的列举和详情提炼。
 */
public class LocalSkillProvider implements CatholicSkillProvider {
    private static final String SKILL_FILE_NAME = "SKILL.md";

    private final Path skillsDirectory;

    public LocalSkillProvider(Path skillsDirectory) {
        this.skillsDirectory = Objects.requireNonNull(skillsDirectory, "skillsDirectory")
            .toAbsolutePath().normalize();
    }

    public LocalSkillProvider(String skillsDirectory) {
        this(Path.of(Objects.requireNonNull(skillsDirectory, "skillsDirectory")));
    }

    @Override
    public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
        return asynchronously(this::readCandidates);
    }

    @Override
    public Future<CatholicSkill> loadSkillByName(String skillName) {
        Objects.requireNonNull(skillName, "skillName");
        return asynchronously(() -> readSkill(skillFileFor(skillName), skillName, true));
    }

    private List<CatholicSkillFrontmatter> readCandidates() {
        if (!Files.exists(skillsDirectory)) return List.of();
        if (!Files.isDirectory(skillsDirectory)) {
            throw new IllegalArgumentException("skills path is not a directory: " + skillsDirectory);
        }

        try (var children = Files.list(skillsDirectory)) {
            List<Path> skillFiles = children
                .filter(Files::isDirectory)
                .map(path -> path.resolve(SKILL_FILE_NAME))
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparing(path -> path.getParent().getFileName().toString()))
                .toList();
            ArrayList<CatholicSkillFrontmatter> candidates = new ArrayList<>(skillFiles.size());
            for (Path skillFile : skillFiles) {
                candidates.add(readSkill(skillFile, skillFile.getParent().getFileName().toString(), false));
            }
            return List.copyOf(candidates);
        } catch (IOException e) {
            throw new IllegalStateException("failed to list skills directory: " + skillsDirectory, e);
        }
    }

    private Path skillFileFor(String skillName) {
        if (!skillName.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("invalid skill name: " + skillName);
        }
        Path skillFile = skillsDirectory.resolve(skillName).resolve(SKILL_FILE_NAME).normalize();
        if (!skillFile.startsWith(skillsDirectory)) {
            throw new IllegalArgumentException("skill escapes skills directory: " + skillName);
        }
        return skillFile;
    }

    private LocalSkill readSkill(Path skillFile, String directoryName, boolean includeInstructions) {
        ParsedDocument document = readDocument(skillFile, includeInstructions);
        Map<String, Object> frontmatter = parseFrontmatter(document.frontmatter(), skillFile);
        String name = requiredString(frontmatter, "name", skillFile);
        if (!directoryName.equals(name)) {
            throw new IllegalArgumentException("skill name '" + name + "' does not match directory '"
                + directoryName + "': " + skillFile);
        }
        String description = requiredString(frontmatter, "description", skillFile);
        String license = optionalString(frontmatter, "license", skillFile);
        String compatibility = optionalString(frontmatter, "compatibility", skillFile);
        String allowedTools = optionalString(frontmatter, "allowed-tools", skillFile);
        Map<String, String> metadata = metadata(frontmatter.get("metadata"), skillFile);
        return new LocalSkill(name, description, license, compatibility, metadata, allowedTools,
            includeInstructions ? document.instructions() : "");
    }

    private static ParsedDocument readDocument(Path skillFile, boolean includeInstructions) {
        try (BufferedReader reader = Files.newBufferedReader(skillFile, StandardCharsets.UTF_8)) {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("\uFEFF")) firstLine = firstLine.substring(1);
            if (!"---".equals(firstLine)) {
                throw new IllegalArgumentException("SKILL.md must start with YAML frontmatter: " + skillFile);
            }

            StringBuilder frontmatter = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.equals("---")) {
                if (!frontmatter.isEmpty()) frontmatter.append('\n');
                frontmatter.append(line);
            }
            if (line == null) throw new IllegalArgumentException("unterminated YAML frontmatter: " + skillFile);
            if (!includeInstructions) return new ParsedDocument(frontmatter.toString(), "");

            StringBuilder instructions = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!instructions.isEmpty()) instructions.append('\n');
                instructions.append(line);
            }
            return new ParsedDocument(frontmatter.toString(), instructions.toString().strip());
        } catch (IOException e) {
            throw new IllegalStateException("failed to read skill file: " + skillFile, e);
        }
    }

    private static Map<String, Object> parseFrontmatter(String yamlSource, Path skillFile) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Object loaded;
        try {
            loaded = new Yaml(new SafeConstructor(options)).load(yamlSource);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("invalid YAML frontmatter: " + skillFile, e);
        }
        if (!(loaded instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("YAML frontmatter must be a mapping: " + skillFile);
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (!(key instanceof String stringKey)) {
                throw new IllegalArgumentException("frontmatter keys must be strings: " + skillFile);
            }
            result.put(stringKey, value);
        });
        return result;
    }

    private static String requiredString(Map<String, Object> map, String key, Path skillFile) {
        String value = optionalString(map, key, skillFile);
        if (value == null) throw new IllegalArgumentException("missing frontmatter field '" + key + "': " + skillFile);
        return value;
    }

    private static @Nullable String optionalString(Map<String, Object> map, String key, Path skillFile) {
        Object value = map.get(key);
        if (value == null) return null;
        if (!(value instanceof String stringValue)) {
            throw new IllegalArgumentException("frontmatter field '" + key + "' must be a string: " + skillFile);
        }
        return stringValue;
    }

    private static Map<String, String> metadata(@Nullable Object value, Path skillFile) {
        if (value == null) return Map.of();
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("frontmatter field 'metadata' must be a mapping: " + skillFile);
        }
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        map.forEach((key, item) -> {
            if (!(key instanceof String stringKey) || !(item instanceof String stringValue)) {
                throw new IllegalArgumentException("metadata keys and values must be strings: " + skillFile);
            }
            metadata.put(stringKey, stringValue);
        });
        return Map.copyOf(metadata);
    }

    private static <T> Future<T> asynchronously(Supplier<T> supplier) {
        Promise<T> promise = Promise.promise();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                promise.complete(supplier.get());
            } catch (Throwable throwable) {
                promise.fail(throwable);
            }
        });
        return promise.future();
    }

    private record ParsedDocument(String frontmatter, String instructions) {}

    private record LocalSkill(String name, String description, @Nullable String license,
                              @Nullable String compatibility, Map<String, String> metadata,
                              @Nullable String allowedTools, String instructions) implements CatholicSkill {}
}
