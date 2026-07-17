package io.github.sinri.keel.aigc.api.agent.skill;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Skill 的基础定义，用于描述一个 Skill 的基本信息。
 */
public interface CatholicSkillFrontmatter {
    String name();
    String description();

    /** Skill 的许可证名称，或指向 Skill 内许可证文件的引用。 */
    default @Nullable String license() {
        return null;
    }

    /** Skill 对产品、系统包、网络等运行环境的要求。 */
    default @Nullable String compatibility() {
        return null;
    }

    /** 扩展元数据；键和值均应为字符串。 */
    default Map<String, String> metadata() {
        return Map.of();
    }

    /**
     * 预授权工具表达式。该字段仍属 Agent Skills 实验特性，保留官方的空格分隔字符串形式。
     */
    default @Nullable String allowedTools() {
        return null;
    }
}
