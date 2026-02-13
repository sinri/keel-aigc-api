package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatVisionContentElement;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.logger.api.LogLevel;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.List;
@NullMarked
class DashscopeMultimodalGenerationServiceTest extends LLMServiceFacadeBasedUnitTest {
    private final DashscopeMultimodalGenerationService dashscopeMultimodalGenerationService;

    DashscopeMultimodalGenerationServiceTest() throws NotConfiguredException {
        var apiKey = ProviderConfigElement.load().dashscope().qwen().apiKey();
        dashscopeMultimodalGenerationService = new DashscopeMultimodalGenerationService(apiKey, getUnitTestLogger());
        getUnitTestLogger().visibleLevel(LogLevel.INFO);
    }
    private MixChatRequest createMixChatRequest() {
        return MixChatRequest.create()
                             .setModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN3_VL_FLASH)
                             .addMessage(msg -> msg
                                     .setRole("user")
                                     .setVisionContent(List.of(
                                             MixChatVisionContentElement.create()
                                                                        .setText("这个图里有什么东西"),
                                             MixChatVisionContentElement.create()
                                                                        .setImage("https://gips1.baidu.com/it/u=3023405515,2440191959&fm=3028&app=3028&f=PNG&fmt=auto&q=100&size=f1138_640")
                                     ))
                             );
    }

    @Test
    void testTextSync(VertxTestContext testContext) {
        dashscopeMultimodalGenerationService.request(createMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp:\n" + resp.toFormattedJsonExpression());
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamChunked(VertxTestContext testContext) {
        dashscopeMultimodalGenerationService.requestStreamRaw(createMixChatRequest(), jsonObject -> {
                                          getUnitTestLogger().info("a chunk comes", x -> x.put("chunk", jsonObject));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamBuffered(VertxTestContext testContext) {
        dashscopeMultimodalGenerationService.requestStreamRaw(createMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }
}