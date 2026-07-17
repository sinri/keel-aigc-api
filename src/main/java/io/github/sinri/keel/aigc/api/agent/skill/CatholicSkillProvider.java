package io.github.sinri.keel.aigc.api.agent.skill;

import io.vertx.core.Future;

import java.util.List;

/**
 * Skill 提供者，用于提供 Skill 的基本信息和完整内容。
 */
public interface CatholicSkillProvider {
    /**
     * 发现当前交互可用的 Skill。实现应只读取用于渐进披露的 frontmatter，不必加载完整正文。
     */
    Future<List<CatholicSkillFrontmatter>> getSkillCandidates();

    /**
     * 在模型决定激活后加载完整 Skill。
     */
    Future<CatholicSkill> loadSkillByName(String skillName);
}
