package io.github.sinri.keel.llm.api.sect.dialect.qwen.request.parameters;

import io.github.sinri.keel.llm.api.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolDefinition;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersToolCallMixin<E> extends QwenRequestParametersCore<E> {
    /**
     * 可供模型调用的工具数组，可以包含一个或多个工具对象。
     * 一次Function Calling流程模型会从中选择一个工具（开启parallel_tool_calls可以选择多个工具）。
     * <p>
     * 目前不支持通义千问VL/Audio，也不建议用于数学和代码模型。
     * </p>
     *
     * @see <a
     *         href=
     *         "https://help.aliyun.com/zh/model-studio/use-qwen-by-calling-api#df816f8ec85ry">parallel_tool_calls</a>
     */
    default E addTool(ToolDefinition toolDefinition) {
        this.ensureJsonArray("tools")
            .add(toolDefinition.toJsonObject());
        return this.getImplementation();
    }

    default List<ToolDefinition> tools() {
        List<JsonObject> x = readJsonObjectArray("tools");
        if (x == null) return List.of();
        return x.stream().map(CommonToolDefinition::new).collect(Collectors.toUnmodifiableList());
    }

    /**
     * 由大模型进行工具策略的选择。
     */
    default E setToolChoiceAsAuto() {
        ensureEntry("tool_choice", "auto");
        return this.getImplementation();
    }

    /**
     * 如果您希望无论输入什么问题，Function Calling 都不会进行工具调用，可以设定tool_choice参数为"none"。
     */
    default E setToolChoiceAsNone() {
        ensureEntry("tool_choice", "none");
        return this.getImplementation();
    }

    /**
     * 如果您希望对于某一类问题，Function Calling 能够强制调用某个工具。
     *
     * @param functionName 指定的工具函数名称
     */
    default E setToolChoiceAsFunction(String functionName) {
        ensureEntry("tool_choice", new JsonObject()
                .put("type", "function")
                .put("function", new JsonObject()
                        .put("name", functionName)));
        return this.getImplementation();
    }

    /**
     * 是否开启并行工具调用。
     *
     * @param parallelToolCalls 参数为true时开启，为false时不开启。
     * @see <a href="https://help.aliyun.com/zh/model-studio/qwen-function-calling#cb6b5c484bt4x">并行工具调用</a>
     */
    default E parallelToolCalls(boolean parallelToolCalls) {
        ensureEntry("parallel_tool_calls", parallelToolCalls);
        return this.getImplementation();
    }

    default @Nullable Boolean parallelToolCalls() {
        return readBoolean("parallel_tool_calls");
    }
}
