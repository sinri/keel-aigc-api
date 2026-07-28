package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.NativeFunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表示以函数名注册和分派 {@link NativeFunctionAdapter} 的工具调用处理器。
 * 它将已注册的原生函数适配器同时作为可提供给 LLM 的工具定义集合和本地执行目标；
 * 收到 {@link CatholicFunctionToolCall} 后解析 JSON 参数、查找同名适配器并异步执行。
 * 所有调用还会通过配置的 {@link CatholicToolInvocationObserver} 产生审计事件。
 */
public class CatholicToolInvocationHandlerWithNativeFunctionAdapters extends CatholicToolInvocationHandler {

    private final Map<String, NativeFunctionAdapter> functionAdapterMap = new ConcurrentHashMap<>();

    public CatholicToolInvocationHandlerWithNativeFunctionAdapters() {
    }

    public CatholicToolInvocationHandlerWithNativeFunctionAdapters(CatholicToolInvocationObserver observer) {
        observedBy(observer);
    }

    public void registerNativeFunctionAdapter(NativeFunctionAdapter functionAdapter) {
        functionAdapterMap.put(functionAdapter.functionName(), functionAdapter);
    }

    public List<CatholicToolDefinition> getRegisteredToolDefinitions() {
        return functionAdapterMap.values().stream()
                                 .map(NativeFunctionAdapter::toCatholicToolDefinition)
                                 .toList();
    }

    @Override
    protected Future<String> handleToolCall(CatholicFunctionToolCall toolCall) {
        NativeFunctionAdapter nativeFunctionAdapter = functionAdapterMap.get(toolCall.functionName());
        if (nativeFunctionAdapter == null) {
            return Future.failedFuture(new IllegalArgumentException(
                    "No native function adapter registered for tool: " + toolCall.functionName()));
        }
        try {
            JsonObject args = toolCall.parseArguments();
            return nativeFunctionAdapter.call(args);
        } catch (RuntimeException e) {
            return Future.failedFuture(e);
        }
    }
}
