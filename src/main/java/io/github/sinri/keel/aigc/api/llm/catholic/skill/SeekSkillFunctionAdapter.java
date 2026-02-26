package io.github.sinri.keel.aigc.api.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.SchemaType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SeekSkillFunctionAdapter implements FunctionAdapter {
    public static final String PARAMETER_SKILL_NAME = "skillName";
    public static final String FUNCTION_NAME = "seekSkill";
    private final SkillProvider skillProvider;

    public SeekSkillFunctionAdapter(SkillProvider skillProvider) {
        this.skillProvider = skillProvider;
    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public String getFunctionDescription() {
        return "seek skill with certain name";
    }

    @Override
    public List<FunctionParameterDefinition> getParameters() {
        return List.of(new FunctionParameterDefinition(
                SchemaType.STRING,
                PARAMETER_SKILL_NAME,
                "the name of a certain skill"
        ));
    }

    @Override
    public Future<String> call(@Nullable JsonObject arguments, @Nullable JsonObject fixedArgument) {
        return Future.succeededFuture()
                     .compose(v -> {
                         if (arguments == null) {
                             return Future.failedFuture("arguments is null");
                         }
                         String skillName = arguments.getString(PARAMETER_SKILL_NAME);
                         return skillProvider.getSkill(skillName);
                     })
                     .compose(skill -> {
                         String rootPath = skill.getRootPath();
                         return skill.getContent()
                                     .compose(skillContent -> {
                                         var s = """
                                                 获取到提供的 Agent Skill，根目录位于 `%s`，其内容如下：
                                                 
                                                 ```
                                                 %s
                                                 ```
                                                 
                                                 请继续作业。
                                                 """
                                                 .formatted(rootPath, skillContent);
                                         return Future.succeededFuture(s);
                                     });
                     });
    }
}
