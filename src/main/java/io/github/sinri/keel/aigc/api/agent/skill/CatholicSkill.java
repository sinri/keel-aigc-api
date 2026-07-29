package io.github.sinri.keel.aigc.api.agent.skill;

import java.util.List;

/**
 * Skill 的完整内容，包括名称、描述、正文内容、各附件等。
 * @see <a href="https://agentskills.io/home">Agent Skills</a>
 */
public interface CatholicSkill extends CatholicSkillFrontmatter {
    /**
     * {@code SKILL.md} 去除 YAML frontmatter 后的 Markdown 指令正文。
     */
    String instructions();

    /**
     * Skill 根目录内可按需读取的资源清单，不包含 {@code SKILL.md}。
     */
    default List<CatholicSkillResource> resources() {
        return List.of();
    }
}
