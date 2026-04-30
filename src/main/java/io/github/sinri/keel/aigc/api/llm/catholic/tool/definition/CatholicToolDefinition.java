package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.vertx.core.Handler;

/**
 * 工具定义的接口，支持多种工具类型（如 function）。
 */
public interface CatholicToolDefinition {
    /**
     * 创建 function 类型的工具定义
     */
    static CatholicToolDefinition function(Handler<FunctionDefinition.Builder> functionBuilding) {
        FunctionDefinition.Builder builder = FunctionDefinition.builder();
        functionBuilding.handle(builder);
        FunctionDefinition functionDefinition = builder.build();
        return function(functionDefinition);
    }

    static CatholicToolDefinition function(FunctionDefinition functionDefinition) {
        return new CatholicFunctionToolDefinitionImpl(functionDefinition);
    }

    /**
     * 工具类型标识。
     * <p>
     * 例如，对于{@link CatholicFunctionToolDefinition}, type 为 function
     */
    String type();

    /**
     * 相应的工具定义。
     * <p>
     * 例如，对于{@link CatholicFunctionToolDefinition}, type 为 function ，返回 FunctionDefinition 对象。
     */
    Object getToolDefinition();
}