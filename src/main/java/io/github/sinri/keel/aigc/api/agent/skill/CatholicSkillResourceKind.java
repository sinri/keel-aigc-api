package io.github.sinri.keel.aigc.api.agent.skill;

/**
 * Agent Skill 中资源的用途分类。
 */
public enum CatholicSkillResourceKind {
    SCRIPT,
    REFERENCE,
    ASSET,
    OTHER;

    public static CatholicSkillResourceKind fromPath(String path) {
        if (path.startsWith("scripts/")) return SCRIPT;
        if (path.startsWith("references/")) return REFERENCE;
        if (path.startsWith("assets/")) return ASSET;
        return OTHER;
    }
}
