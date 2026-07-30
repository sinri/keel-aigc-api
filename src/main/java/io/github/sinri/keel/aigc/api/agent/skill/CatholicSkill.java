package io.github.sinri.keel.aigc.api.agent.skill;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Skill 的完整内容，包括名称、描述、正文内容、各附件等。
 *
 * @see <a href="https://agentskills.io/home">Agent Skills</a>
 */
public interface CatholicSkill extends CatholicSkillFrontmatter {
    /**
     * {@code SKILL.md} 去除 YAML frontmatter 后的 Markdown 指令正文。
     */
    String instructions();

    /**
     * Skill 在本地文件系统中的根目录。
     * <p>
     * 本地 Provider 应返回绝对路径，使 Agent 在调用文件系统工具时可以直接解析
     * {@link #resources()} 中的相对路径。
     * <p>
     * 远程或不以文件系统为后端的 Provider 可以保留默认的 {@code null}，表示
     * Skill 未缓存在本地，并通过 {@code read_skill_resource} 提供资源；如果已在
     * 本地缓存，则可以返回缓存根目录的绝对路径。
     */
    default @Nullable Path directory() {
        return null;
    }

    /**
     * Skill 根目录内可按需读取的资源清单，不包含 {@code SKILL.md}。
     */
    default List<CatholicSkillResource> resources() {
        return List.of();
    }
}
