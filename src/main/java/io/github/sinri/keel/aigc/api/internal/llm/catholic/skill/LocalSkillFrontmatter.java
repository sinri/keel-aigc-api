package io.github.sinri.keel.aigc.api.internal.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.skill.SkillFrontmatter;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SkillFrontmatter} 的不可变实现，基于 SKILL.md 文件中的 YAML frontmatter 解析结果构建。
 * <p>
 * SKILL.md 文件格式示例：
 * <pre>{@code
 * ---
 * name: my-skill
 * description: A skill that does something useful
 * license: MIT
 * compatibility: cursor
 * allowed-tools: runCommand readFile
 * metadata:
 *   author: someone
 *   version: 1.0
 * ---
 * # Markdown body here
 * }</pre>
 *
 * @see <a href="https://agentskills.io/specification#frontmatter-required">Frontmatter (required)</a>
 */
class LocalSkillFrontmatter implements SkillFrontmatter {
    private final String name;
    private final String description;
    private final @Nullable String license;
    private final @Nullable String compatibility;
    private final @Nullable Map<String, String> metadata;
    private final @Nullable String allowedTools;

    /**
     * @param name          Skill 名称（必填）
     * @param description   Skill 描述（必填）
     * @param license       许可证（可选）
     * @param compatibility 兼容性说明（可选）
     * @param metadata      额外的键值对元数据（可选）
     * @param allowedTools  空格分隔的预批准工具列表（可选，实验性）
     */
    LocalSkillFrontmatter(
            String name,
            String description,
            @Nullable String license,
            @Nullable String compatibility,
            @Nullable Map<String, String> metadata,
            @Nullable String allowedTools
    ) {
        this.name = name;
        this.description = description;
        this.license = license;
        this.compatibility = compatibility;
        this.metadata = metadata;
        this.allowedTools = allowedTools;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public @Nullable String getLicense() {
        return license;
    }

    @Override
    public @Nullable String getCompatibility() {
        return compatibility;
    }

    @Override
    public @Nullable Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public @Nullable String getAllowedTools() {
        return allowedTools;
    }

    /**
     * 从 SKILL.md 的完整文本内容中提取并解析 YAML frontmatter。
     * <p>
     * 文本必须以 {@code ---} 开头，frontmatter 以第二个 {@code ---} 结束。
     * 使用 snakeyaml 解析 YAML 部分，提取各字段并构建 {@link LocalSkillFrontmatter} 实例。
     *
     * @param content SKILL.md 文件的完整文本内容
     * @return 解析后的 {@link LocalSkillFrontmatter} 实例
     * @throws IllegalArgumentException 若 frontmatter 格式不合法或缺少必填字段
     */
    static LocalSkillFrontmatter parseFromContent(String content) {
        String yamlText = extractFrontmatterYaml(content);

        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(yamlText);
        if (yamlMap == null) {
            throw new IllegalArgumentException("SKILL.md frontmatter is empty");
        }

        String name = requireString(yamlMap, "name");
        String description = requireString(yamlMap, "description");
        String license = optionalString(yamlMap, "license");
        String compatibility = optionalString(yamlMap, "compatibility");
        String allowedTools = optionalString(yamlMap, "allowed-tools");
        Map<String, String> metadata = optionalStringMap(yamlMap, "metadata");

        return new LocalSkillFrontmatter(name, description, license, compatibility, metadata, allowedTools);
    }

    /**
     * 从 SKILL.md 全文中截取两个 {@code ---} 之间的 YAML 文本。
     */
    private static String extractFrontmatterYaml(String content) {
        String trimmed = content.stripLeading();
        if (!trimmed.startsWith("---")) {
            throw new IllegalArgumentException("SKILL.md must start with '---' frontmatter delimiter");
        }
        int secondDelimiter = trimmed.indexOf("---", 3);
        if (secondDelimiter < 0) {
            throw new IllegalArgumentException("SKILL.md missing closing '---' frontmatter delimiter");
        }
        return trimmed.substring(3, secondDelimiter).strip();
    }

    private static String requireString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("SKILL.md frontmatter missing required field: " + key);
        }
        return value.toString();
    }

    private static @Nullable String optionalString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

//    @SuppressWarnings("unchecked")
    private static @Nullable Map<String, String> optionalStringMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("SKILL.md frontmatter field '" + key + "' must be a mapping");
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return Map.copyOf(result);
    }
}
