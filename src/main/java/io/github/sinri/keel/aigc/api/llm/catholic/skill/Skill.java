package io.github.sinri.keel.aigc.api.llm.catholic.skill;

import io.vertx.core.Future;

import java.io.File;

public interface Skill {
    default String getName() {
        return getFrontmatter().getName();
    }

    default String getDescription() {
        return getFrontmatter().getDescription();
    }

    SkillFrontmatter getFrontmatter();

    String getRootPath();

    /**
     *
     * @return skill.md 全文
     */
    Future<String> getContent();

    default String getScriptsPath() {
        return getRootPath() + File.separator + "scripts";
    }

    default String getScriptsPath(String targetFile) {
        return getScriptsPath() + File.separator + targetFile;
    }

    default String getReferencesPath() {
        return getRootPath() + File.separator + "references";
    }

    default String getReferencesPath(String targetFile) {
        return getReferencesPath() + File.separator + targetFile;
    }

    default String getAssetsPath() {
        return getRootPath() + File.separator + "assets";
    }

    default String getAssetsPath(String targetFile) {
        return getAssetsPath() + File.separator + targetFile;
    }

    SkillStub getSkillStub();
}
