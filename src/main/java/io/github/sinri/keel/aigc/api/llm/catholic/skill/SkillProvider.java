package io.github.sinri.keel.aigc.api.llm.catholic.skill;

import io.vertx.core.Future;

import java.util.List;

public interface SkillProvider {
    Future<List<SkillStub>> getSkillStubs();

    Future<Skill> getSkill(String name);
}
