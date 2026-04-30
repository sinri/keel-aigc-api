package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.NativeFunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatholicToolInvocationHandlerWithNativeFunctionAdapters implements CatholicToolInvocationHandler {

    private final Map<String, NativeFunctionAdapter> functionAdapterMap = new ConcurrentHashMap<>();

    public void registerNativeFunctionAdapter(NativeFunctionAdapter functionAdapter) {
        functionAdapterMap.put(functionAdapter.functionName(), functionAdapter);
    }

    public List<CatholicToolDefinition> getRegisteredToolDefinitions() {
        return functionAdapterMap.values().stream()
                                 .map(NativeFunctionAdapter::toCatholicToolDefinition)
                                 .toList();
    }

    @Override
    public Future<String> handle(CatholicFunctionToolCall toolCall) {
        NativeFunctionAdapter nativeFunctionAdapter = functionAdapterMap.get(toolCall.functionName());
        JsonObject args = toolCall.parseArguments();
        return nativeFunctionAdapter.call(args);
    }
}
