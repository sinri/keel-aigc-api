package io.github.sinri.keel.aigc.api.agent.skill;

import java.util.Objects;

/**
 * Skill 脚本执行结果。
 */
public record CatholicSkillScriptResult(int exitCode, String stdout, String stderr) {
    public CatholicSkillScriptResult {
        Objects.requireNonNull(stdout, "stdout");
        Objects.requireNonNull(stderr, "stderr");
    }
}
