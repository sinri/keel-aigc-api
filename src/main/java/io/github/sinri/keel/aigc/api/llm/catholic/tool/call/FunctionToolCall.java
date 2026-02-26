package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 函数工具调用接口。
 * <p>
 * 表示 LLM 对函数工具的调用，包含函数名称和参数信息。
 *
 * @since 5.0.0
 */
public interface FunctionToolCall {
    /**
     * 获取调用函数的名称。
     *
     * @return 函数名称，
     * @throws NullPointerException 如果未设置函数名称
     */
    String getName() throws NullPointerException;

    /**
     * 获取需要输入到函数中的参数，为 JSON 字符串。
     * <p>
     * 由于大模型响应有一定随机性，输出的 JSON 字符串并不总满足于您的函数，
     * 建议您在将参数输入函数前进行参数的有效性校验。
     *
     * @return 函数参数 JSON 字符串，如果未设置则返回 null
     */
    @Nullable String getArguments();

    /**
     * 获取需要输入到函数中的参数并为 JSON Object 形式。
     *
     * @return JSON Object 形式的函数入参。如果未设置参数或者空白则返回 null。
     * @throws DecodeException 如果参数 JSON 字符串无法解析为 JSON Object
     */
    default @Nullable JsonObject getArgumentsAsJsonObject() throws DecodeException {
        String arguments = getArguments();
        if (arguments == null || arguments.isBlank()) return null;
        return new JsonObject(arguments);
    }

    /**
     * 转换为 JSON 对象。
     *
     * @return JSON 对象
     */
    JsonObject toJsonObject();
}
