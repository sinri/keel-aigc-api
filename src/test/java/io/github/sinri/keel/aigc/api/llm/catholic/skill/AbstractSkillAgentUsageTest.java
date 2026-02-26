package io.github.sinri.keel.aigc.api.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionToolCall;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeMultimodalLargeLanguageModel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSkillAgentUsageTest extends LLMServiceFacadeBasedUnitTest {
    public AbstractSkillAgentUsageTest() throws NotConfiguredException {
        super();
    }

    abstract protected SkillProvider buildSkillProvider();

    protected Future<Void> oneShot(String userPrompt) {
        var skillProvider = buildSkillProvider();

        SeekSkillFunctionAdapter seekSkillFunctionAdapter = new SeekSkillFunctionAdapter(skillProvider);
        RunCommandFunctionAdapter runCommandFunctionAdapter = new RunCommandFunctionAdapter(getKeel());
        LLMServiceFacade.registerFunctionAdapter(seekSkillFunctionAdapter);
        LLMServiceFacade.registerFunctionAdapter(runCommandFunctionAdapter);

        List<MixChatMessage> context = new ArrayList<>();

        return skillProvider
                .getSkillStubs()
                .compose(skillStubs -> {
                    StringBuilder sb = new StringBuilder();
                    for (var s : skillStubs) {
                        sb.append("* ").append("Skill Name: ").append(s.name()).append("\n");
                        sb.append("\t- description: ").append(s.description()).append("\n");
                    }
                    String systemPrompt = """
                                          你需要响应用户的要求，按照要求回答或代用户处理事务。
                                          对于用户的要求，优先查询是否已经存在现成的skill，如存在应查阅执行。
                                          已有的 Skills 如下：
                                          
                                          %s
                                          """.formatted(sb.toString());
                    context.add(MixChatMessage.create()
                                              .setRole("system")
                                              .setTextContent(systemPrompt)
                    );
                    context.add(MixChatMessage.create()
                                              .setRole("user")
                                              .setTextContent(userPrompt)
                    );

                    return getKeel().asyncCallRepeatedly(repeatedlyCallTask -> {
                        MixChatRequest request = MixChatRequest.create()
                                                               .setModel(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3D5_PLUS);
                        context.forEach(request::addMessage);
                        request.addTool(seekSkillFunctionAdapter.toToolDefinition())
                               .addTool(runCommandFunctionAdapter.toToolDefinition());

                        getUnitTestLogger().info("to request:\n" + request.toJsonExpression());

                        return LLMServiceFacade
                                .request(request)
                                .compose(response -> {
                                    getUnitTestLogger().info("response:\n" + response);
                                    MixChatMessage message = response.getMessage();
                                    context.add(message);

                                    if (message.getToolCalls().isEmpty()) {
                                        // no tool call required, as the final response
                                        getUnitTestLogger().info("FIN:\n" + message.getTextContent());
                                        repeatedlyCallTask.stop();
                                        return Future.succeededFuture();
                                    } else {
                                        // run each tool calls, and add tool result to context
                                        return getKeel().asyncCallIteratively(message.getToolCalls(), toolCall -> {
                                            String type = toolCall.getType();
                                            if (!Objects.equals(type, "function")) {
                                                return Future.failedFuture("function only");
                                            }
                                            FunctionToolCall function = toolCall.getFunction();
                                            getUnitTestLogger().info("to call function " + function.getName() + "(" + function.getArguments() + ")");
                                            return LLMServiceFacade.callRegisteredFunction(
                                                                           function.getName(),
                                                                           function.getArgumentsAsJsonObject(),
                                                                           null
                                                                   )
                                                                   .compose(fcResult -> {
                                                                       getUnitTestLogger().info("function called:\n" + fcResult);
                                                                       context.add(MixChatMessage.create()
                                                                                                 .setToolCallId(toolCall.getId())
                                                                                                 .setRole("tool")
                                                                                                 .setTextContent(fcResult)
                                                                       );
                                                                       return Future.succeededFuture();
                                                                   });
                                        });
                                    }
                                });
                    });
                });
    }
}
