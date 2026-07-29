package io.github.sinri.keel.aigc.api.agent.skill;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * 按需读取的 Skill 资源内容。
 */
public record CatholicSkillResourceContent(
        String path,
        @Nullable String mediaType,
        byte[] bytes
) {
    public CatholicSkillResourceContent {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(bytes, "bytes");
        bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
}
