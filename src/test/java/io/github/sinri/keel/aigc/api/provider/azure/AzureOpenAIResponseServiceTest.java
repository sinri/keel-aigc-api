package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.llm.MixChatRequestMock;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

@NullMarked
class AzureOpenAIResponseServiceTest extends LLMServiceFacadeBasedUnitTest implements MixChatRequestMock {
    private final AzureOpenAIResponseService azureOpenAIResponseService;

    public AzureOpenAIResponseServiceTest() throws NotConfiguredException {
        AzureOpenAIProviderConfigElement openai = ProviderConfigElement.load().azure().openai();

        this.azureOpenAIResponseService = new AzureOpenAIResponseService(openai, getUnitTestLogger());
    }

    @Test
    void testResponseV1Api(VertxTestContext testContext) {
        azureOpenAIResponseService.request(
                                          createPlainMixChatRequest()
                                  )
                                  .compose(resp -> {
                                      getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                                      return Future.succeededFuture();
                                  })
                                  .onComplete(testContext.succeedingThenComplete());
    }

    @Override
    public String getModelForMixChatRequest() {
        return "gpt-5-chat";
    }
}