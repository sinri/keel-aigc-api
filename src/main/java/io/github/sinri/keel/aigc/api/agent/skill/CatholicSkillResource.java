package io.github.sinri.keel.aigc.api.agent.skill;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Skill 资源的轻量描述。路径始终相对于 Skill 根目录，使用 {@code /} 分隔。
 */
public record CatholicSkillResource(
        String path,
        CatholicSkillResourceKind kind,
        long size,
        @Nullable String mediaType
) {
    public CatholicSkillResource {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(kind, "kind");
        if (path.isBlank() || path.startsWith("/") || path.indexOf('\\') >= 0
                || path.equals("SKILL.md")) {
            throw new IllegalArgumentException("invalid skill resource path: " + path);
        }
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("invalid skill resource path: " + path);
            }
        }
        if (size < 0) throw new IllegalArgumentException("resource size must not be negative");
        if (mediaType != null && mediaType.isBlank()) {
            throw new IllegalArgumentException("resource mediaType must not be blank");
        }
    }
}
