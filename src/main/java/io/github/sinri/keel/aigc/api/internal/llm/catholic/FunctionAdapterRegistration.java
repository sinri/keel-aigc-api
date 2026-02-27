package io.github.sinri.keel.aigc.api.internal.llm.catholic;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FunctionAdapterRegistration {
    private static final FunctionAdapterRegistration instance = new FunctionAdapterRegistration();
    private final Map<String, FunctionAdapter> fcMap = new ConcurrentHashMap<>();

    private FunctionAdapterRegistration() {

    }

    public static FunctionAdapterRegistration getInstance() {
        return instance;
    }

    public void registerFunctionAdapter(FunctionAdapter functionAdapter) {
        fcMap.put(functionAdapter.getFunctionName(), functionAdapter);
    }

    public @Nullable FunctionAdapter getFunctionAdapter(String functionName) {
        return fcMap.get(functionName);
    }

    public Future<String> callRegisteredFunction(String functionName, @Nullable JsonObject arguments, @Nullable JsonObject fixedArguments) {
        FunctionAdapter functionAdapter = getFunctionAdapter(functionName);
        if (functionAdapter == null) {
            return Future.failedFuture(new NullPointerException("No function adapter for " + functionName + " in FunctionAdapterRegistration"));
        }
        return functionAdapter.call(arguments, fixedArguments);
    }

    public Map<String, FunctionAdapter> getFunctionAdapters() {
        return Collections.unmodifiableMap(fcMap);
    }
}
