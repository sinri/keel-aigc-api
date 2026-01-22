package io.github.sinri.keel.llm.api.sect.provider.dashscope;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.request.QwenRequest;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.UUID;
@NullMarked
class DashscopeProviderTest extends KeelJUnit5Test {

    private final DashscopeProvider dashscopeProvider;

    public DashscopeProviderTest(VertxTestContext testContext) throws NotConfiguredException {
        System.out.println("RTOC: " + rtoc);
        System.out.println("RTOC Vertx: " + rtoc.vertx());
        LLMServiceFacade.getInstance().setVertx(rtoc.vertx());
        System.out.println("LLMServiceFacade Vertx Set");
        var apiKey = ProviderConfigElement.load().dashscope().qwen().apiKey();
        getUnitTestLogger().info("Qwen API Key Got");
        dashscopeProvider = new DashscopeProvider(apiKey, getUnitTestLogger());
        testContext.completeNow();
    }

    private QwenRequest createQwenRequest() {
        return QwenRequest.create()
                          .input(input -> input
                                  .addSystemMessage("你是一个同声传译，用户说中文，你翻译成日文")
                                  .addUserChatMessage("今天的生活也是一样的苦涩")
                          );
    }

    @Test
    void testTextSync(VertxTestContext testContext) {
        var webClient = LLMServiceFacade.getInstance().getWebClient();
        getUnitTestLogger().info("LLMServiceFacade WebClient Got");

        dashscopeProvider.request(
                                 webClient,
                                 "qwen-plus",
                                 createQwenRequest().toJsonObject(),
                                 UUID.randomUUID().toString()
                         )
                         .compose(resp -> {
                             getUnitTestLogger().info("resp:\n" + resp.encodePrettily());
                             return Future.succeededFuture();
                         })
                         .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testTextStreamChunked(VertxTestContext testContext) {
        Vertx vertx = LLMServiceFacade.getInstance().getVertx();
        var httpClient = LLMServiceFacade.getInstance().getHttpClient();

        dashscopeProvider.requestStream(
                                 vertx,
                                 httpClient,
                                 "qwen-flash",
                                 createQwenRequest()
                                         .parameters(p -> p.stream(true).incrementalOutput(true))
                                         .toJsonObject(),
                                 s -> {
                                     getUnitTestLogger().info("IN CHUNK:\n" + s);
                                     return Future.succeededFuture();
                                 },
                                 180_000L,
                                 UUID.randomUUID().toString()
                         )
                         .onComplete(testContext.succeedingThenComplete());
    }

}