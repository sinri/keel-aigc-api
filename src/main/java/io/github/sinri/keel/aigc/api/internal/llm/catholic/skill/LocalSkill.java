package io.github.sinri.keel.aigc.api.internal.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillFrontmatter;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.vertx.core.Future;

/**
 * 基于本地文件系统目录的 Skill 接口实现。
 *
 * @see <a href="https://agentskills.io/specification">The complete format specification for Agent Skills.</a>
 */
public class LocalSkill implements Skill {
    private final String rootPath;

    /**
     * 通过本地文件系统中的一个 Skill 目录路径，构建一个 Skill 实例。
     * <p>
     * A skill is a directory containing at minimum a {@code SKILL.md} file,
     * and Optional directories:
     * {@code scripts/},
     * {@code references/},
     * and {@code assets/}.
     *
     * @param rootPath Skill 对应的目录路径。
     */
    public LocalSkill(String rootPath) {
        this.rootPath=rootPath;
    }

    @Override
    public SkillFrontmatter getFrontmatter() {
        // todo
    }

    @Override
    public final String getRootPath() {
        return rootPath;
    }

    /**
     * 该 Skill 目录下 SKILL.md 文件的内容。
     */
    @Override
    public Future<String> getContent() {
        // todo
    }

    @Override
    public SkillStub getSkillStub() {
        // todo
    }
}
