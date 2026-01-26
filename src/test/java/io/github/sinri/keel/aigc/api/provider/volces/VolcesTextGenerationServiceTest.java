package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.sect.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.llm.MixChatRequestMock;
import io.vertx.core.Future;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@NullMarked
class VolcesTextGenerationServiceTest extends LLMServiceFacadeBasedUnitTest implements MixChatRequestMock {
    private final VolcesTextGenerationService service;

    VolcesTextGenerationServiceTest() throws NotConfiguredException {
        VolcesConfigElement volcesConfigElement = ProviderConfigElement.load().volces();
        this.service = new VolcesTextGenerationService(volcesConfigElement, getUnitTestLogger());
    }


    @BeforeAll
    public static void beforeAll() {
        LLMServiceFacade.setKeel(rtoc.vertx());
    }

    @Test
    @Timeout(180_000)
    void testRequest(VertxTestContext testContext) {
        service.request(
                       createPlainMixChatRequest()
               )
               .compose(resp -> {
                   getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @Timeout(180_000)
    void testRequestStreamRawChunked(VertxTestContext testContext) {
        service.requestStreamRaw(
                       createPlainMixChatRequest(),
                       entries -> {
                           getUnitTestLogger().info("chunk", x -> x.put("chunk", entries));
                           return Future.succeededFuture();
                       }
               )
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @Timeout(180_000)
    void testRequestStreamRawBuffered(VertxTestContext testContext) {
        service.requestStreamRaw(
                       createPlainMixChatRequest()
               )
               .compose(resp -> {
                   getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                   return Future.succeededFuture();
               })
               .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @Timeout(180_000)
    void testRequestStream(VertxTestContext testContext) {
        service.requestStream(
                       createPlainMixChatRequest(),
                       entries -> {
                           getUnitTestLogger().info("chunk", x -> x.put("chunk", entries));
                           return Future.succeededFuture();
                       }
               )
               .onComplete(testContext.succeedingThenComplete());
    }

    @Override
    public String getModelForMixChatRequest() {
        return VolcesLargeLanguageModel.MODEL_CODE_DOUBAO_PRO_32K;
    }
}