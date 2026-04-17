package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

public class DashScopeTextGenerationClientChatStreamTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("dashscope.test1.api");
        String apiKey = ConfigElement.root().readProperty("dashscope.test1.key");
        String model = ConfigElement.root().readProperty("dashscope.test1.model");

        Objects.requireNonNull(apiKey, "DashScope API key must be set");
        Objects.requireNonNull(model, "DashScope model must be set");

        if (baseUrl == null) {
            baseUrl = "https://dashscope.aliyuncs.com/api/v1";
        }

        DashScopeTextGenerationClient client = DashScopeTextGenerationClient.builder()
                .httpClient(httpClient)
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful travel assistant."))
                .addMessage(CatholicUserMessage.ofText("请帮我制定一个下个月从中国到日本的旅行计划。"))
                .enableStream()
                .build();

        getLogger().info("Sending stream request to DashScope Text Generation API (collecting all chunks)...");

        return client.callStream(request)
                .compose(response -> {
                    getLogger().info("Response ID: " + response.id());

                    if (response.hasText()) {
                        getLogger().info("Generated Travel Plan:\n" + response.text());
                    }

                    var usage = response.usage();
                    if (usage.totalTokens() != null) {
                        getLogger().info("Token Usage: input=" + usage.promptTokens() +
                                ", output=" + usage.completionTokens() +
                                ", total=" + usage.totalTokens());
                    }

                    getLogger().info("Stream completed, all chunks collected.");

                    return Future.succeededFuture();
                });
    }
}