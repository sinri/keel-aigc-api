package io.github.sinri.keel.aigc.api.llm.rag;

import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeMultimodalLargeLanguageModel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

@NullMarked
public class AlphaRagUsageTest extends LLMServiceFacadeBasedUnitTest {

    public AlphaRagUsageTest() throws NotConfiguredException {
        super();
    }

    @Test
    void test1(VertxTestContext testContext) {
        RagInstruction instruction = new RagInstructionImpl();
        RagProviderImpl provider = new RagProviderImpl(instruction);

        String question = "Now which machine is still working?";
        provider.getRagEnrichedPrompt(question)
                .compose(enrichedPrompt -> {
                    getUnitTestLogger().info("enrichedPrompt:\n" + enrichedPrompt);

                    MixChatRequest request = MixChatRequest.create()
                                                           .setModel(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3D5_PLUS)
                                                           .addMessage(msg -> msg
                                                                   .setRole("system")
                                                                   .setTextContent("Answer the question based on the provide context.")
                                                           )
                                                           .addMessage(msg -> msg
                                                                   .setRole("user")
                                                                   .setTextContent(enrichedPrompt)
                                                           );
                    return LLMServiceFacade.request(request)
                                           .compose(resp -> {
                                               getUnitTestLogger().info("resp:\n" + resp.toJsonExpression());
                                               return Future.succeededFuture();
                                           });
                })
                .onComplete(testContext.succeedingThenComplete());
    }
}
