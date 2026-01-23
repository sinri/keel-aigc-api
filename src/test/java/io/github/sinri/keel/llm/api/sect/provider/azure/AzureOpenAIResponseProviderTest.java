package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatRequest;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@NullMarked
class AzureOpenAIResponseProviderTest extends LLMServiceFacadeBasedUnitTest {
    private final AzureOpenAIResponseProvider azureOpenAIResponseProvider;

    public AzureOpenAIResponseProviderTest() throws NotConfiguredException {
        this.azureOpenAIResponseProvider = new AzureOpenAIResponseProvider(
                ProviderConfigElement.load().azure().openai(),
                getUnitTestLogger()
        );
    }

    private StatefulChatRequest createStatefulChatRequest() {
        return StatefulChatRequest.create()
                                  .setModel("gpt-5-chat")
                                  .setInput("已知公司持有 GoDaddy 上的域名，但相关账号纷失，付款记录不可考，要如何处理？");
    }

    @Test
    void testRequest(VertxTestContext testContext) {
        azureOpenAIResponseProvider.request(
                                           LLMServiceFacade.getWebClient(),
                                           "gpt-5-chat",
                                           createStatefulChatRequest().toJsonObject(),
                                           UUID.randomUUID().toString()
                                   )
                                   .compose(resp -> {
                                       getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                                       return Future.succeededFuture();
                                   })
                                   .onComplete(testContext.succeedingThenComplete());
    }
}