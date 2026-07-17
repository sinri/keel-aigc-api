package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkill;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillFrontmatter;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.NativeFunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration.DashScopeMultimodalGenerationLLM;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientAgent;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CatholicAgent 渐进加载 Skill，并由 Skill 指令引导工具调用的示例。
 */
public class CatholicAgentSkillSampleTest extends KeelInstantRunner {
    private static final String WEATHER_SKILL_NAME = "weather-reporting";

    private String getBaseUrl() {
        return Objects.requireNonNull(ConfigElement.root().readProperty("dashscope.test2.api"));
    }

    private String getApiKey() {
        return Objects.requireNonNull(ConfigElement.root().readProperty("dashscope.test2.key"));
    }

    private String getModel() {
        return Objects.requireNonNull(ConfigElement.root().readProperty("dashscope.test2.model"));
    }

    @Override
    protected Future<Void> run() {
        HttpClientAgent httpClientAgent = getKeel().httpClientBuilder()
                                                   .with(new HttpClientOptions()
                                                       .setSsl(true)
                                                       .setKeepAlive(true))
                                                   .build();
        DashScopeMultimodalGenerationLLM llm = DashScopeMultimodalGenerationLLM.builder()
                                                                              .keel(getKeel())
                                                                              .baseUrl(getBaseUrl())
                                                                              .apiKey(getApiKey())
                                                                              .httpClient(httpClientAgent)
                                                                              .build();

        var toolInvocationHandler = CatholicToolInvocationHandler.createWithNativeFunctionAdapters();
        toolInvocationHandler.registerNativeFunctionAdapter(new QueryWeatherFCAdapter());

        CatholicAgent agent = CatholicAgent.builder()
                                           .llm(llm)
                                           .model(getModel())
                                           .tools(toolInvocationHandler.getRegisteredToolDefinitions())
                                           .toolHandler(toolInvocationHandler)
                                           .skillProvider(new WeatherSkillProvider())
                                           .maxRounds(4)
                                           .build();

        return agent.interact("今天是 2026 年 4 月 4 日，请按天气播报规范告诉我杭州明天的天气。")
                    .compose(result -> {
                        getLogger().info("termination: " + result.termination());
                        getLogger().info("llm rounds: " + result.llmRounds());
                        getLogger().info("tool rounds: " + result.toolRounds());
                        getLogger().info("resp:\n" + result.text());
                        return Future.succeededFuture();
                    });
    }

    private static class WeatherSkillProvider implements CatholicSkillProvider {
        @Override
        public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
            return Future.succeededFuture(List.of(new CatholicSkillFrontmatter() {
                @Override
                public String name() {
                    return WEATHER_SKILL_NAME;
                }

                @Override
                public String description() {
                    return "查询天气并生成规范天气播报。当天气查询、天气预报或天气播报被请求时使用。";
                }
            }));
        }

        @Override
        public Future<CatholicSkill> loadSkillByName(String skillName) {
            if (!WEATHER_SKILL_NAME.equals(skillName)) {
                return Future.failedFuture("Unknown skill: " + skillName);
            }
            return Future.succeededFuture(new CatholicSkill() {
                @Override
                public String name() {
                    return WEATHER_SKILL_NAME;
                }

                @Override
                public String description() {
                    return "查询天气并生成规范天气播报。当天气查询、天气预报或天气播报被请求时使用。";
                }

                @Override
                public String instructions() {
                    return """
                        # 天气播报流程

                        1. 从用户请求中确认地点和具体日期。
                        2. 必须调用 `query_weather` 获取天气，不得自行编造天气数据。
                        3. 用中文回答，并明确包含地点、日期、天气情况和气温。
                        4. 如果天气不利于出行，补充一句简短的出行建议。
                        """;
                }
            });
        }
    }

    private static class QueryWeatherFCAdapter implements NativeFunctionAdapter {
        @Override
        public String functionName() {
            return "query_weather";
        }

        @Override
        public String functionDescription() {
            return "Query weather info";
        }

        @Override
        public Map<String, ParameterDefinition> parameterDefinitionMap() {
            Map<String, ParameterDefinition> map = new HashMap<>();
            ParameterDefinition location = new ParameterDefinition(
                "location", "string", "the place to query weather, such as city name", true);
            ParameterDefinition day = new ParameterDefinition(
                "day", "string", "the day to check weather", true);
            map.put(location.name(), location);
            map.put(day.name(), day);
            return map;
        }

        @Override
        public Future<String> call(@Nullable JsonObject fixedArgs, JsonObject args) {
            String location = args.getString("location");
            String day = args.getString("day");
            LoggerFactory.getShared().createLogger(getClass())
                         .info("query weather: " + location + " on " + day);
            return Future.succeededFuture(
                "天气情况：" + location + "在" + day + "下雨，气温 30 摄氏度。");
        }
    }
}
