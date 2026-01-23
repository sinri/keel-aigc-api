package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.llm.api.sect.provider.MixChatRequestMock;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@NullMarked
class AzureOpenAIResponseServiceTest extends KeelJUnit5Test implements MixChatRequestMock {
    private final AzureOpenAIResponseService azureOpenAIResponseService;

    public AzureOpenAIResponseServiceTest() throws NotConfiguredException {
        OpenAIConfigElement openai = ProviderConfigElement.load().azure().openai();

        this.azureOpenAIResponseService = new AzureOpenAIResponseService(openai, getUnitTestLogger());
    }

    @BeforeAll
    public static void beforeAll() {
        LLMServiceFacade.getInstance().setVertx(rtoc.vertx());
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