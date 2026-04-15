package io.github.sinri.keel.aigc.api.provider.vertex;

import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

class VertexGeminiProviderTest extends LLMServiceFacadeBasedUnitTest {

    private final VertexGeminiProvider vertexGeminiProvider;

    public VertexGeminiProviderTest() throws NotConfiguredException {
        super();
        VertexProjectConfigElement vertexProjectConfigElement = ProviderConfigElement.load().vertex()
                                                                                     .getProject("default");
        this.vertexGeminiProvider = new VertexGeminiProvider(vertexProjectConfigElement, getUnitTestLogger());
    }

    @Test
    @Timeout(value = 180, timeUnit = TimeUnit.SECONDS)
    void testStream(VertxTestContext testContext) {
        JsonObject payload = new JsonObject()
                .put("contents", new JsonArray()
                        .add(new JsonObject()
                                .put("role", "user")
                                .put("parts", new JsonObject()
                                        .put("text", "描述如何运用 Java 的 HttpClient 来调用 Vertex AI 的 Gemini 模型生成接口")
                                )
                        )
                );
        vertexGeminiProvider.requestStream(
                                    getKeel(),
                                    getHttpClientForLLM(),
                                    "gemini-2.5-flash-lite",
                                    payload,
                                    fragment -> {
                                        getUnitTestLogger().info("IN CHUNK:\n" + fragment);
                                        JsonObject object = new JsonObject(fragment);
                                        getUnitTestLogger().info(x -> x.message("CHUNK as OBJECT").context("object", object));
                                        return Future.succeededFuture();
                                    },
                                    180_000L,
                                    UUID.randomUUID().toString()
                            )
                            .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @Timeout(value = 180, timeUnit = TimeUnit.SECONDS)
    void test(VertxTestContext testContext) {
        JsonObject payload = new JsonObject()
                .put("contents", new JsonArray()
                        .add(new JsonObject()
                                .put("role", "user")
                                .put("parts", new JsonObject()
                                        .put("text", "描述如何运用 Java 的 HttpClient 来调用 Vertex AI 的 Gemini 模型生成接口")
                                )
                        )
                );
        vertexGeminiProvider.request(getWebClientForLLM(),
                                    "gemini-2.5-flash-lite",
                                    payload,
                                    UUID.randomUUID().toString()
                            )
                            .compose(j -> {
                                getUnitTestLogger().info("resp:\n" + j.encodePrettily());
                                return Future.succeededFuture();
                            })
                            .onComplete(testContext.succeedingThenComplete());
    }
}