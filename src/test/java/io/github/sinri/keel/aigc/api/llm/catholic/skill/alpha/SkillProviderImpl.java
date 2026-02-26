package io.github.sinri.keel.aigc.api.llm.catholic.skill.alpha;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.List;
import java.util.Objects;

@NullMarked
class SkillProviderImpl implements SkillProvider {
    private final GetCurrentIpSkill getCurrentIpSkill = new GetCurrentIpSkill();

    private String getSkillsDir() {
        String r = ConfigElement.root().readProperty("runtime_dir");
        Objects.requireNonNull(r, "The runtime directory is null!");
        return r + File.separator + "skills";
    }

    @Override
    public Future<List<SkillStub>> getSkillStubs() {
        return Future.succeededFuture(List.of(
                getCurrentIpSkill.getSkillStub()
        ));
    }

    @Override
    public Future<Skill> getSkill(String name) {
        if (Objects.equals(name, "get-current-ip")) {
            return Future.succeededFuture(getCurrentIpSkill);
        } else {
            return Future.failedFuture("do not found skill");
        }
    }
}
