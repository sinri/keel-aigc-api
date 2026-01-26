package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.sect.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request.DoubaoRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@NullMarked
class VolcesProviderTest extends LLMServiceFacadeBasedUnitTest {
    private final VolcesProvider provider;

    public VolcesProviderTest() throws NotConfiguredException {
        VolcesConfigElement volcesConfigElement = ProviderConfigElement.load().volces();
        provider = new VolcesProvider(volcesConfigElement, getUnitTestLogger());
    }


    private DoubaoRequest createDoubaoRequest() {
        return DoubaoRequest.create()
                            .addSystemChatMessage("你是一个同声传译，用户说中文，你翻译成日文。不要输出多余的内容。")
                            .addUserChatMessage("今天的生活也是一样的苦涩");
    }

    @Test
    void request(VertxTestContext testContext) {
        WebClient webClient = LLMServiceFacade.getWebClient();
        provider.request(
                        webClient,
                        VolcesLargeLanguageModel.MODEL_CODE_DOUBAO_PRO_32K,
                        createDoubaoRequest().toJsonObject(),
                        UUID.randomUUID().toString()
                )
                .compose(resp -> {
                    getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                    return Future.succeededFuture();
                })
                .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void requestStream(VertxTestContext testContext) {
        Vertx vertx = LLMServiceFacade.getKeel();
        HttpClient httpClient = LLMServiceFacade.getHttpClient();
        provider.requestStream(
                        vertx,
                        httpClient,
                        VolcesLargeLanguageModel.MODEL_CODE_DOUBAO_PRO_32K,
                        createDoubaoRequest().stream(true).toJsonObject(),
                        s -> {
                            getUnitTestLogger().info("fragment", x -> x.put("fragment", s));
                            return Future.succeededFuture();
                        },
                        180_000L,
                        UUID.randomUUID().toString()
                )
                .onComplete(testContext.succeedingThenComplete());
    }
}