package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration.DashScopeMultimodalGenerationLLM;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientAgent;
import io.vertx.core.http.HttpClientOptions;

public class CatholicAgentSampleTest extends KeelInstantRunner {
    private String getBaseUrl() {
        return ConfigElement.root().readProperty("dashscope.test2.api");
    }

    private String getApiKey() {
        return ConfigElement.root().readProperty("dashscope.test2.key");
    }

    private String getModel() {
        return ConfigElement.root().readProperty("dashscope.test2.model");
    }

    private CatholicToolDefinition getToolA() {
        return CatholicToolDefinition.function(
                "function_name",
                "function_description"
        );
    }

    @Override
    protected Future<Void> run() throws Exception {
        HttpClientAgent httpClientAgent = getKeel().httpClientBuilder()
                                                   .with(new HttpClientOptions()
                                                           .setSsl(true)
                                                           .setKeepAlive(true))
                                                   .build();
        DashScopeMultimodalGenerationLLM llm = DashScopeMultimodalGenerationLLM.builder()
                                                                               .baseUrl(getBaseUrl())
                                                                               .apiKey(getApiKey())
                                                                               .httpClient(httpClientAgent)
                                                                               .build();

        CatholicToolDefinition toolA = getToolA();
        CatholicAgent.builder()
                     .llm(llm)
                     .model(getModel())
                     .addTool(toolA)
                     .maxToolRounds(3);

        // todo: 还没整明白，放着不动，等下再想
        return null;
    }
}
