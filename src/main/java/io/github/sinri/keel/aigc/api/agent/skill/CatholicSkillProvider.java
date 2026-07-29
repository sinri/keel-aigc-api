package io.github.sinri.keel.aigc.api.agent.skill;

import io.github.sinri.keel.aigc.api.internal.agent.skill.LocalSkillProvider;
import io.vertx.core.Future;

import java.nio.file.Path;
import java.util.List;

/**
 * Skill 提供者，用于提供 Skill 的基本信息和完整内容。
 */
public interface CatholicSkillProvider {

    static CatholicSkillProvider createLocalSkillProvider(Path skillsDirectory) {
        return new LocalSkillProvider(skillsDirectory);
    }

    /**
     * 创建带自定义资源限制的本地 Provider。
     */
    static CatholicSkillProvider createLocalSkillProvider(
            Path skillsDirectory, int maxResourceCount, long maxResourceBytes) {
        return new LocalSkillProvider(skillsDirectory, maxResourceCount, maxResourceBytes);
    }

    /**
     * 发现当前交互可用的 Skill。实现应只读取用于渐进披露的 frontmatter，不必加载完整正文。
     */
    Future<List<CatholicSkillFrontmatter>> getSkillCandidates();

    /**
     * 在模型决定激活后加载完整 Skill。
     */
    Future<CatholicSkill> loadSkillByName(String skillName);

    /**
     * 按相对于 Skill 根目录的路径读取资源。实现必须拒绝绝对路径、目录逃逸及越界符号链接。
     */
    default Future<CatholicSkillResourceContent> readSkillResource(String skillName, String relativePath) {
        return Future.failedFuture(new UnsupportedOperationException(
                "skill resources are not supported by this provider"));
    }
}
