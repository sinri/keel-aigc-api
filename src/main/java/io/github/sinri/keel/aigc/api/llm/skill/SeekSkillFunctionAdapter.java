package io.github.sinri.keel.aigc.api.llm.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.SchemaType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 技能查找函数适配器，用于在 LLM 模型中调用技能查找功能。
 * <p>
 * 如果你需要使用 Agent Skill 协议能力，
 * 可以指定 {@link SkillProvider} 实例来构建此类的实例，
 * 并将其注册到 {@link LLMServiceFacade} 中，
 * 并在请求中
 * （1）将此实例作为 Tool 添加；
 * （2）在请求的 system prompt 中要求使用 Skill。
 *
 * @since 5.0.0
 */
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
                                                 Required Agent Skill is fetched, located at path `%s`, its content is:
                                                 
                                                 ```
                                                 %s
                                                 ```
                                                 
                                                 Continue your job.
                                                 """
                                                 .formatted(rootPath, skillContent);
                                         return Future.succeededFuture(s);
                                     });
                     });
    }
}
