package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.sect.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.llm.MixChatRequestMock;
import io.github.sinri.keel.logger.api.LogLevel;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
@NullMarked
class DashscopeTextGenerationServiceTest extends LLMServiceFacadeBasedUnitTest implements MixChatRequestMock {
    private final DashscopeTextGenerationService dashscopeTextGenerationService;

    DashscopeTextGenerationServiceTest() throws NotConfiguredException {

        var apiKey = ProviderConfigElement.load().dashscope().qwen().apiKey();
        dashscopeTextGenerationService = new DashscopeTextGenerationService(apiKey, getUnitTestLogger());
        getUnitTestLogger().visibleLevel(LogLevel.INFO);
    }

    @BeforeAll
    public static void beforeAll() {
        LLMServiceFacade.setKeel(rtoc.vertx());
    }

    @Override
    public String getModelForMixChatRequest() {
        return DashscopeLargeLanguageModel.MODEL_CODE_QWEN_FLASH;
    }

    @Test
    void testTextSync(VertxTestContext testContext) {
        dashscopeTextGenerationService.request(createPlainMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp:\n" + resp.toFormattedJsonExpression());
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamChunked(VertxTestContext testContext) {
        dashscopeTextGenerationService.requestStreamRaw(createPlainMixChatRequest(), jsonObject -> {
                                          getUnitTestLogger().info("a chunk comes", x -> x.put("chunk", jsonObject));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamBuffered(VertxTestContext testContext) {
        dashscopeTextGenerationService.requestStreamRaw(createPlainMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

}