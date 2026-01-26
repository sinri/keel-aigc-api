package io.github.sinri.keel.aigc.api.llm;

import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.ToolDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.SchemaType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public interface MixChatRequestMock {
    String getModelForMixChatRequest();

    default MixChatRequest createPlainMixChatRequest() {
        return MixChatRequest.create()
                             .setModel(getModelForMixChatRequest())
                             .addMessage(msg -> msg
                                     .setRole("system")
                                     .setTextContent("你是一个同声传译，用户说中文，你翻译成日文")
                             )
                             .addMessage(msg -> msg
                                     .setRole("user")
                                     .setTextContent(
                                             """
                                             賣炭翁，伐薪燒炭南山中。
                                             滿面塵灰煙火色，兩鬢蒼蒼十指黑。
                                             賣炭得錢何所營？身上衣裳口中食。
                                             可憐身上衣正單，心憂炭賤願天寒！
                                             夜來城外一尺雪，曉駕炭車輾冰轍。
                                             牛困人飢日已高，市南門外泥中歇。
                                             翩翩兩騎來是誰？黃衣使者白衫兒。
                                             手把文書口稱敕，迴車叱牛牽向北。
                                             一車炭重千余斤，宮使驅將惜不得！
                                             半匹紅紗一丈綾，繫向牛頭充炭直！
                                             """
                                     )
                             );
    }

    default FunctionAdapter createFunctionAdapter() {
        return new FunctionAdapter() {
            @Override
            public String getFunctionName() {
                return "translateToJapanese";
            }

            @Override
            public String getFunctionDescription() {
                return "将输入内容翻译成日文";
            }

            @Override
            public List<FunctionParameterDefinition> getParameters() {
                return List.of(
                        new FunctionParameterDefinition(
                                SchemaType.STRING,
                                "source_language",
                                "输入内容的语言类型，例如：zh-CN、en-US等"
                        ),
                        new FunctionParameterDefinition(
                                SchemaType.STRING,
                                "source_text",
                                "输入内容文本"
                        )
                );
            }

            @Override
            public Future<String> call(@Nullable JsonObject arguments, @Nullable JsonObject fixedArgument) {
                Objects.requireNonNull(arguments);
                var sl = arguments.getString("source_language");
                var st = arguments.getString("source_text");
                return Future.succeededFuture("Mock translated text from " + sl + ": " + st);
            }
        };
    }

    default MixChatRequest createFCMixChatRequest(List<ToolDefinition> toolDefinitions) {
        var req = MixChatRequest.create()
                                .setModel(getModelForMixChatRequest())
                                .addMessage(msg -> msg
                                        .setRole("system")
                                        .setTextContent("你是需要根据用户的输入内容，识别其对应的语言，如果不是日语的话调用相应工具翻译成日文输出")
                                )
                                .addMessage(msg -> msg
                                        .setRole("user")
                                        .setTextContent("賣炭翁，伐薪燒炭南山中。")
                                );
        toolDefinitions.forEach(req::addTool);
        return req;
    }
}
