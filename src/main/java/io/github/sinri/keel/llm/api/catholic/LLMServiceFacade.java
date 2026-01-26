package io.github.sinri.keel.llm.api.catholic;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.llm.api.internal.catholic.FunctionAdapterRegistration;
import io.github.sinri.keel.llm.api.internal.catholic.LLMRegistration;
import io.github.sinri.keel.llm.api.internal.catholic.LLMServiceFacadeInternal;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.provider.azure.AzureOpenAILargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.provider.dashscope.DashscopeLargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.provider.volces.VolcesLargeLanguageModel;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public final class LLMServiceFacade {
    private static final LLMServiceFacadeInternal INSTANCE = new LLMServiceFacadeInternal();

    public static void registerLLMsFollowingConfig() throws NotConfiguredException {
        ProviderConfigElement p = ProviderConfigElement.load();
        try {
            var apiKey = p.dashscope().qwen().apiKey();
            DashscopeLargeLanguageModel.createCommonQwenSeriesLLMs()
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

    public static @Nullable FunctionAdapter getFunctionAdapter(String functionName) {
        return FunctionAdapterRegistration.getInstance().getFunctionAdapter(functionName);
    }

    public static Future<String> callRegisteredFunction(String functionName, @Nullable JsonObject arguments, @Nullable JsonObject fixedArguments) {
        return FunctionAdapterRegistration.getInstance()
                                          .callRegisteredFunction(functionName, arguments, fixedArguments);
    }
}
