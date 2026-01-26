package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.request.GPTRequest;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@NullMarked
class AzureOpenAIClassicProviderTest extends LLMServiceFacadeBasedUnitTest {
    private final AzureOpenAIClassicProvider provider;

    public AzureOpenAIClassicProviderTest() throws NotConfiguredException {
        OpenAIConfigElement openai = ProviderConfigElement.load().azure().openai();
        provider = new AzureOpenAIClassicProvider(openai, getUnitTestLogger());
    }

    private GPTRequest createGPTRequest() {
        return GPTRequest.create()
                         .addSystemMessage("你是一个同声传译，用户说中文，你翻译成日文")
                         .addUserMessage("今天的生活也是一样的苦涩");
    }

    @Test
    void request(VertxTestContext testContext) {
        var webClient = LLMServiceFacade.getWebClient();

        provider.request(
                        webClient,
                        AzureOpenAILargeLanguageModel.MODEL_CODE_GPT_5_CHAT,
                        createGPTRequest().toJsonObject(),
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
                        AzureOpenAILargeLanguageModel.MODEL_CODE_GPT_5_CHAT,
                        createGPTRequest()
                                .stream(true)
                                .toJsonObject(),
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