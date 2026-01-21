package io.github.sinri.keel.llm.api.sect.dashscope;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.logger.api.LogLevel;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DashscopeTextGenerationServiceTest extends KeelJUnit5Test {
    private final DashscopeTextGenerationService dashscopeTextGenerationService;

    DashscopeTextGenerationServiceTest() throws NotConfiguredException {

        var apiKey = ProviderConfigElement.load().dashscope().qwen().apiKey();
        dashscopeTextGenerationService = new DashscopeTextGenerationService(apiKey, getUnitTestLogger());
        getUnitTestLogger().visibleLevel(LogLevel.INFO);
    }

    @BeforeAll
    public static void beforeAll() {
        LLMServiceFacade.getInstance().setVertx(rtoc.vertx());
    }

    private MixChatRequest createMixChatRequest() {
        return MixChatRequest.create()
                             .setModel("qwen-flash")
                             .addMessage(msg -> msg
                                     .setRole("system")
                                     .setTextContent("你是一个同声传译，用户说中文，你翻译成日文")
                             )
                             .addMessage(msg -> msg
                                     .setRole("user")
                                     .setTextContent("太君，这是我们找到的八路的情报")
                             );
    }

    @Test
    void testTextSync(VertxTestContext testContext) {
        dashscopeTextGenerationService.request(createMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp:\n" + resp.toFormattedJsonExpression());
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamChunked(VertxTestContext testContext) {
        dashscopeTextGenerationService.requestStreamRaw(createMixChatRequest(), jsonObject -> {
                                          getUnitTestLogger().info("a chunk comes", x -> x.put("chunk", jsonObject));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamBuffered(VertxTestContext testContext) {
        dashscopeTextGenerationService.requestStreamRaw(createMixChatRequest())
                                      .compose(resp -> {
                                          getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                                          return Future.succeededFuture();
                                      })
                                      .onComplete(testContext.succeedingThenComplete());
    }

}