package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.internal.llm.catholic.FunctionAdapterRegistration;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.LLMRegistration;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.LLMServiceFacadeInternal;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.provider.azure.AzureOpenAILargeLanguageModel;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeMultimodalLargeLanguageModel;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeTextLargeLanguageModel;
import io.github.sinri.keel.aigc.api.provider.volces.VolcesLargeLanguageModel;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * LLM 服务门面类。
 * <p>
 * 提供统一的静态方法接口，用于管理 LLM 模型注册、函数适配器注册，以及执行聊天请求。
 * <p>
 * 该类使用单例模式管理内部状态，包括 Keel 实例、Web 客户端、HTTP 客户端和日志记录器。
 *
 * @since 5.0.0
 */
public final class LLMServiceFacade {
    private static final LLMServiceFacadeInternal INSTANCE = new LLMServiceFacadeInternal();

    /**
     * 根据配置文件注册所有可用的 LLM 模型。
     * <p>
     * 本方法应在使用{@link LLMServiceFacade}实例的功能之前调用。
     *
     * @throws NotConfiguredException 如果配置文件未找到或未正确配置
     */
    public static void registerLLMsFollowingConfig() throws NotConfiguredException {
        ProviderConfigElement p = ProviderConfigElement.load();
        try {
            var apiKey = p.dashscope().qwen().apiKey();
            DashscopeTextLargeLanguageModel.createCommonQwenSeriesLLMs()
                                           .forEach(LLMServiceFacade::registerModel);
            DashscopeMultimodalLargeLanguageModel.createCommonQwenSeriesLLMs()
                                                 .forEach(LLMServiceFacade::registerModel);
        } catch (NotConfiguredException ignored) {
        }
        try {
            p.azure().openai().getChildNames().forEach(llmRegisterCode -> {
                registerModel(new AzureOpenAILargeLanguageModel(llmRegisterCode));
            });
        } catch (NotConfiguredException ignored) {
        }

        try {
            String apiKey = p.volces().apiKey();
            ConfigElement model = p.volces().extract("model");
            model.getChildNames().forEach(llmRegisterCode -> {
                registerModel(new VolcesLargeLanguageModel(llmRegisterCode));
            });
        } catch (NotConfiguredException ignored) {
        }
    }

    public static Future<MixChatResponse> request(MixChatRequest mixChatRequest) {
        return INSTANCE.request(mixChatRequest);
    }

    public static Future<Void> requestStreamRaw(MixChatRequest mixChatRequest, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        return INSTANCE.requestStreamRaw(mixChatRequest, fragmentDataHandler);
    }

    public static Future<MixChatResponse> requestStreamRaw(MixChatRequest mixChatRequest) {
        return INSTANCE.requestStreamRaw(mixChatRequest);
    }

    public static Future<Void> requestStream(MixChatRequest mixChatRequest, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        return INSTANCE.requestStream(mixChatRequest, chunkHandler);
    }

    public static LargeLanguageModel getModelWithCode(String llmRegisterCode) {
        return LLMRegistration.shared().getModelWithCode(llmRegisterCode);
    }

    public static void registerModel(LargeLanguageModel serviceSpecification) {
        LLMRegistration.shared().registerModel(serviceSpecification);
    }

    public static Keel getKeel() {
        return INSTANCE.getKeel();
    }

    public static void setKeel(Keel keel) {
        INSTANCE.setKeel(keel);
    }

    public static void setKeel(Vertx vertx) {
        if (vertx instanceof Keel keel) {
            INSTANCE.setKeel(keel);
        } else {
            INSTANCE.setKeel(Keel.create(vertx));
        }
    }

    public static WebClient getWebClient() {
        return INSTANCE.getWebClient();
    }

    public static HttpClient getHttpClient() {
        return INSTANCE.getHttpClient();
    }

    public static Logger getLogger() {
        return INSTANCE.getLogger();
    }

    public static void registerFunctionAdapter(FunctionAdapter functionAdapter) {
        FunctionAdapterRegistration.getInstance().registerFunctionAdapter(functionAdapter);
    }

    public static Map<String, FunctionAdapter> getRegisteredFunctionAdapterMap() {
        return FunctionAdapterRegistration.getInstance().getFunctionAdapters();
    }

    public static List<ToolDefinition> getRegisteredToolDefinitions() {
        return getRegisteredFunctionAdapterMap().values().stream().map(FunctionAdapter::toToolDefinition).toList();
    }

    public static @Nullable FunctionAdapter getFunctionAdapter(String functionName) {
        return FunctionAdapterRegistration.getInstance().getFunctionAdapter(functionName);
    }

    public static Future<String> callRegisteredFunction(String functionName, @Nullable JsonObject arguments, @Nullable JsonObject fixedArguments) {
        return FunctionAdapterRegistration.getInstance()
                                          .callRegisteredFunction(functionName, arguments, fixedArguments);
    }

    private LLMServiceFacade() {
        // singleton
    }
}
