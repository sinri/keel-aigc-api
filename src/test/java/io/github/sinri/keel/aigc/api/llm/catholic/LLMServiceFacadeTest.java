package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.MixChatRequestMock;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionToolCall;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeLargeLanguageModel;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

@NullMarked
class LLMServiceFacadeTest extends LLMServiceFacadeBasedUnitTest implements MixChatRequestMock {

    public LLMServiceFacadeTest() throws NotConfiguredException {
    }


    @Override
    public String getModelForMixChatRequest() {
        return DashscopeLargeLanguageModel.MODEL_CODE_QWEN_PLUS;
    }

    @Test
    void test1(VertxTestContext testContext) {
        FunctionAdapter functionAdapter = createFunctionAdapter();
        LLMServiceFacade.request(createFCMixChatRequest(List.of(functionAdapter.toToolDefinition())))
                        .compose(resp -> {
                            getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                            return Future.succeededFuture();
                        })
                        .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void test2(VertxTestContext testContext) {
        FunctionAdapter functionAdapter = createFunctionAdapter();
        LLMServiceFacade.request(createFCMixChatRequest(List.of(functionAdapter.toToolDefinition())))
                        .compose(resp -> {
                            getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                            testContext.verify(() -> {
                                Assertions.assertFalse(resp.getMessage().getToolCalls().isEmpty());
                            });
                            var toolCall = resp.getMessage().getToolCalls().get(0);
                            testContext.verify(() -> {
                                Assertions.assertEquals("function", toolCall.getType());
                            });
                            FunctionToolCall function = toolCall.getFunction();
                            Assertions.assertNotNull(function);
                            String name = function.getName();
                            testContext.verify(() -> {
                                Assertions.assertEquals(functionAdapter.getFunctionName(), name);
                            });
                            String arguments = function.getArguments();
                            Assertions.assertNotNull(arguments);
                            return functionAdapter.call(new JsonObject(arguments), null);
                        })
                        .compose(s -> {
                            getUnitTestLogger().info("fc: " + s);
                            return Future.succeededFuture();
                        })
                        .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void test3(VertxTestContext testContext) {
        FunctionAdapter functionAdapter = createFunctionAdapter();
        LLMServiceFacade.registerFunctionAdapter(functionAdapter);
        LLMServiceFacade.request(createFCMixChatRequest(List.of(functionAdapter.toToolDefinition())))
                        .compose(resp -> {
                            getUnitTestLogger().info("resp", x -> x.put("resp", resp));

                            getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                            testContext.verify(() -> {
                                Assertions.assertFalse(resp.getMessage().getToolCalls().isEmpty());
                            });
                            var toolCall = resp.getMessage().getToolCalls().get(0);
                            testContext.verify(() -> {
                                Assertions.assertEquals("function", toolCall.getType());
                            });
                            FunctionToolCall function = toolCall.getFunction();
                            Assertions.assertNotNull(function);
                            String name = function.getName();
                            Objects.requireNonNull(name);
                            testContext.verify(() -> {
                                Assertions.assertEquals(functionAdapter.getFunctionName(), name);
                            });
                            String arguments = function.getArguments();
                            Assertions.assertNotNull(arguments);

                            return LLMServiceFacade.callRegisteredFunction(name, new JsonObject(arguments), null);
                        })
                        .compose(s -> {
                            getUnitTestLogger().info("fc: " + s);
                            return Future.succeededFuture();
                        })
                        .onComplete(testContext.succeedingThenComplete());
    }
}