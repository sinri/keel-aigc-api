package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.agent.tool.CatholicToolInvocationHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.NativeFunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
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
import java.util.Map;
import java.util.Objects;

public class CatholicAgentSampleTest extends KeelInstantRunner {
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
        DashScopeMultimodalGenerationLLM.Builder builder = DashScopeMultimodalGenerationLLM.builder();
        DashScopeMultimodalGenerationLLM llm = builder.keel(getKeel())
                                                      .baseUrl(getBaseUrl())
                                                      .apiKey(getApiKey())
                                                      .httpClient(httpClientAgent)
                                                      .build();

        QueryWeatherFCAdapter queryWeatherFcAdapter = new QueryWeatherFCAdapter();

        var toolInvocationHandler = CatholicToolInvocationHandler.createWithNativeFunctionAdapters();
        toolInvocationHandler.registerNativeFunctionAdapter(queryWeatherFcAdapter);

        CatholicAgent agent = CatholicAgent.builder()
                                           .llm(llm)
                                           .model(getModel())
                                           .tools(toolInvocationHandler.getRegisteredToolDefinitions())
                                           .toolHandler(toolInvocationHandler)
                                           .maxRounds(3)
                                           .build();

        return agent.chat("今天是 2026年 4 月 4 日，杭州明天的天气怎么样")
                    .compose(catholicLLMResponse -> {
                        getLogger().info("resp:\n" + catholicLLMResponse.text());
                        return Future.succeededFuture();
                    });

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

            ParameterDefinition p1 = new ParameterDefinition("location", "string", "the place to query weather, such as city name", true);
            ParameterDefinition p2 = new ParameterDefinition("day", "string", "the day to check weather", true);

            map.put(p1.name(), p1);
            map.put(p2.name(), p2);

            return map;
        }

        @Override
        public Future<String> call(@Nullable JsonObject fixedArgs, JsonObject args) {
            String location = args.getString("location");
            String day = args.getString("day");

            LoggerFactory.getShared().createLogger(getClass()).info("query weather: " + location + " on " + day);

            return Future.succeededFuture("天气情况：" + location + "在" + day + "下雨，气温 30 摄氏度。");
        }
    }
}
