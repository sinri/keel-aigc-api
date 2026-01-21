package io.github.sinri.keel.llm.api.catholic.tool.call;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface FunctionToolCall {
    /**
     * 调用函数的名称
     */
    @Nullable String getName();

    /**
     * 需要输入到函数中的参数，为JSON字符串。
     * 由于大模型响应有一定随机性，输出的JSON字符串并不总满足于您的函数，建议您在将参数输入函数前进行参数的有效性校验。
     */
    @Nullable String getArguments();

    JsonObject toJsonObject();
}
