package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

/**
 * 工具定义的接口，支持多种工具类型（如 function）。
 */
public interface CatholicToolDefinition {
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