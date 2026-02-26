package io.github.sinri.keel.aigc.api.llm.catholic.tool;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 函数适配器接口。
 * <p>
 * 用于将业务函数适配为 LLM 可以调用的工具函数，提供函数名称、描述、参数定义等信息，
 * 并实现函数调用逻辑。
 *
 * @since 5.0.0
 */
public interface FunctionAdapter {
    /**
     * 获取工具名称。
     *
     * @return 工具名称
     */
    String getFunctionName();

    /**
     * 获取工具描述。
     *
     * @return 工具描述
     */
    String getFunctionDescription();

    /**
     * 获取工具参数列表。
     *
     * @return 工具参数列表
     */
    List<FunctionParameterDefinition> getParameters();

    /**
     * 将工具定义转换为 CommonFunctionToolDefinition 对象。
     *
     * @return CommonFunctionToolDefinition 对象
     */
    default FunctionToolDefinition toFunctionToolDefinition() {
        return new CommonFunctionToolDefinition(
                getFunctionName(),
                getFunctionDescription(),
                getParameters()
        );
    }

    /**
     * 将工具定义转换为 CommonToolDefinition 对象。
     *
     * @return CommonToolDefinition 对象
     */
    default ToolDefinition toToolDefinition() {
        return new CommonToolDefinition(toFunctionToolDefinition());
    }

    /**
     * Call function as LLM required, along with a fixed argument according to running context.
     *
     * @param arguments     the arguments parsed from LLM response
     * @param fixedArgument the argument object from context
     * @return the execute result of this function
     */
    Future<String> call(@Nullable JsonObject arguments, @Nullable JsonObject fixedArgument);

}
