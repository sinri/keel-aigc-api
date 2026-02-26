package io.github.sinri.keel.aigc.api.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.internal.llm.catholic.skill.LocalSkillProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;

public interface SkillProvider {
    static SkillProvider withLocal(Vertx vertx, String skillsDirPath) {
        return new LocalSkillProvider(vertx, skillsDirPath);
    }

    Future<List<SkillStub>> getSkillStubs();

    Future<Skill> getSkill(String name);
}
