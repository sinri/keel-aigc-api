package io.github.sinri.keel.aigc.api.internal.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.vertx.core.Future;

import java.util.List;

/**
 * SkillProvider 的基于本地文件系统目录的实现。
 *
 * @see <a href="https://agentskills.io/what-are-skills">What are skills</a>
 */
public class LocalSkillProvider implements SkillProvider {
    private final String skillsDirPath;

    /**
     * 通过给定的一个存放各种 Skill 目录的目录来初始化。
     *
     * @param skillsDirPath Skills 目录路径。
     */
    public LocalSkillProvider(String skillsDirPath) {
        this.skillsDirPath = skillsDirPath;
    }

    /**
     * 从指定目录（{@link LocalSkillProvider#skillsDirPath}）加载所有 Skill 并构成 Skill 存根列表返回。
     *
     * @return Skill 存根列表
     */
    @Override
    public Future<List<SkillStub>> getSkillStubs() {
        // todo
    }

    /**
     * 根据 Skill 名称，从文件系统中找对应目录，读取其中的 SKILL.md 文件内容来建立 Skill 类实例（如 {@link LocalSkill}）。
     *
     * @param name Skill 名称
     * @return Skill 实例
     */
    @Override
    public Future<Skill> getSkill(String name) {
        // todo
    }
}
