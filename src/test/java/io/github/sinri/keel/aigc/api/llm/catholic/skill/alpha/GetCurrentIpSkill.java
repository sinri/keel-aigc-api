package io.github.sinri.keel.aigc.api.llm.catholic.skill.alpha;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillFrontmatter;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Objects;

@NullMarked
class GetCurrentIpSkill implements Skill {
    private final SkillFrontmatter skillFrontmatter = new SkillFrontmatter() {
        @Override
        public String getName() {
            return "get-current-ip";
        }

        @Override
        public String getDescription() {
            return "To get current IP of this device on Internet by a certain API.";
        }

        @Override
        public @Nullable String getLicense() {
            return null;
        }

        @Override
        public @Nullable String getCompatibility() {
            return null;
        }

        @Override
        public @Nullable Map<String, String> getMetadata() {
            return null;
        }

        @Override
        public @Nullable String getAllowedTools() {
            return null;
        }
    };

    public GetCurrentIpSkill(){
        super();
    }
    @Override
    public SkillFrontmatter getFrontmatter() {
        return skillFrontmatter;
    }

    private String getSkillsDir() {
        String r = ConfigElement.root().readProperty("runtime_dir");
        Objects.requireNonNull(r, "The runtime directory is null!");
        return r + File.separator + "skills";
    }

    @Override
    public String getRootPath() {
        return getSkillsDir() + File.separator + "get-current-ip";
    }

    @Override
    public Future<String> getContent() {
        String s = """
                   ---
                   name: %s
                   description: %s
                   ---
                   
                   # Get Current IP
                   
                   ## When to use this skill
                   
                   If you need to know the current IP of this device on Internet.
                   
                   ## Steps
                   
                   Use Tool Call `runCommand`, with the two parameters:
                   
                   * command: fixed as `curl https://sinri.cc/api/WelcomeController/apiGetMeta`
                   * dir: set as the dir of this skill
                   
                   Get the result of tool call, trim the spaces in the front and the end of the output,
                   then parse the rest string as a JSON object, read follow the JSON Path `$.data.ip`,
                   eventually get the current IP of this device on Internet.
                   
                   """
                .formatted(
                        skillFrontmatter.getName(),
                        skillFrontmatter.getDescription()
                );
        return Future.succeededFuture(s);
    }

    @Override
    public SkillStub getSkillStub() {
        return new SkillStub(getName(), getDescription(), getRootPath());
    }
}
