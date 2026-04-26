package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

public class DashScopeTextGenerationClientChatStreamChunkTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("dashscope.test1.api");
        String apiKey = ConfigElement.root().readProperty("dashscope.test1.key");
        String model = ConfigElement.root().readProperty("dashscope.test1.model");

        Objects.requireNonNull(apiKey, "DashScope API key must be set");
        Objects.requireNonNull(model, "DashScope model must be set");

        if (baseUrl == null) {
            baseUrl = "https://dashscope.aliyuncs.com/api/v1";
        }

        DashScopeTextGenerationLLM client = DashScopeTextGenerationLLM.builder()
                                                                      .httpClient(httpClient)
                                                                      .apiKey(apiKey)
                                                                      .baseUrl(baseUrl)
                                                                      .build();

        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful travel assistant."))
                .addMessage(CatholicUserMessage.ofText("输出一个俳句，描绘 AI Vibe Coding 的两面性"))
                .enableStream()
                .build();

        getLogger().info("Sending stream request to DashScope Text Generation API (processing chunks)...");

        return client.callStream(request, chunk -> {
            if (chunk.id() != null) {
                getLogger().info("Chunk ID: " + chunk.id());
            }

            if (chunk.hasDeltaText()) {
                getLogger().info("Delta text: " + chunk.deltaText());
            }

            if (chunk.hasDeltaToolCalls()) {
                getLogger().info("Delta tool calls: " + chunk.deltaToolCalls().size());
            }

            if (chunk.isFinished()) {
                getLogger().info("Stream finished!");

                var usage = chunk.usage();
                if (usage.promptTokens() != null) {
                    getLogger().info("Token Usage: input=" + usage.promptTokens() +
                            ", output=" + usage.completionTokens() +
                            ", total=" + usage.totalTokens());
                }
            }

            return Future.succeededFuture();
        });
    }
}