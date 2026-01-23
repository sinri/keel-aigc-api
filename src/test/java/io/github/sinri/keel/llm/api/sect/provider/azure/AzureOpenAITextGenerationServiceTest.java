package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.llm.api.MixChatRequestMock;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

@NullMarked
class AzureOpenAITextGenerationServiceTest extends LLMServiceFacadeBasedUnitTest implements MixChatRequestMock {
    private final AzureOpenAITextGenerationService service;

    AzureOpenAITextGenerationServiceTest() throws NotConfiguredException {
        OpenAIConfigElement openai = ProviderConfigElement.load().azure().openai();

        service = new AzureOpenAITextGenerationService(openai, getUnitTestLogger());
    }

    @Override
    public String getModelForMixChatRequest() {
        return AzureOpenAILargeLanguageModel.MODEL_CODE_GPT_5_CHAT;
    }

    @Test
    void testRequest(VertxTestContext testContext) {
        MixChatRequest plainMixChatRequest = createPlainMixChatRequest();
        service.request(plainMixChatRequest)
               .compose(resp -> {
                   getUnitTestLogger().info("resp", x -> x.put("resp", resp.cloneAsJsonObject()));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }


    @Test
    void testRequestStreamRawChunked(VertxTestContext testContext) {
        MixChatRequest plainMixChatRequest = createPlainMixChatRequest();
        service.requestStreamRaw(plainMixChatRequest, j -> {
                   getUnitTestLogger().info("chunk", x -> x.put("chunk", j));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testRequestStreamRawBuffered(VertxTestContext testContext) {
        MixChatRequest plainMixChatRequest = createPlainMixChatRequest();
        service.requestStreamRaw(plainMixChatRequest)
               .compose(resp -> {
                   getUnitTestLogger().info("resp", x -> x.put("resp", resp.cloneAsJsonObject()));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testRequestStreamChunked(VertxTestContext testContext) {
        MixChatRequest plainMixChatRequest = createPlainMixChatRequest();
        service.requestStream(plainMixChatRequest, chunk -> {
                   getUnitTestLogger().info("chunk", x -> x.put("chunk", chunk));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testRequestStreamBuffered(VertxTestContext testContext) {
        MixChatRequest plainMixChatRequest = createPlainMixChatRequest();
        service.requestStream(plainMixChatRequest)
               .compose(resp -> {
                   getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }
}