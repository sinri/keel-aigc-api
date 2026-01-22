package io.github.sinri.keel.llm.api.catholic;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.internal.catholic.LLMRegistration;
import io.github.sinri.keel.llm.api.sect.provider.azure.AzureOpenAILargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.provider.dashscope.DashscopeLargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.provider.volces.VolcesLargeLanguageModel;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

public final class LLMServiceFacade implements LLMService, LLMRegistration {
    private static final LLMServiceFacade INSTANCE = new LLMServiceFacade();
    private final LateObject<Vertx> lateVertx = new LateObject<>();
    private final LateObject<WebClient> lateWebClient = new LateObject<>();
    private final LateObject<HttpClient> lateHttpClient = new LateObject<>();
    private final LateObject<Logger> lateLogger = new LateObject<>();

    private LLMServiceFacade() {
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN3_MAX));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN_PLUS));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN_FLASH));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN_LONG));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN3_VL_PLUS));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN3_VL_FLASH));
        registerModel(new DashscopeLargeLanguageModel(DashscopeLargeLanguageModel.MODEL_CODE_QWEN_VL_OCR));

        registerModel(new AzureOpenAILargeLanguageModel(AzureOpenAILargeLanguageModel.MODEL_CODE_GPT_5_CHAT));

        registerModel(new VolcesLargeLanguageModel(VolcesLargeLanguageModel.MODEL_CODE_DOUBAO_PRO_32K));
    }

    public static LLMServiceFacade getInstance() {
        return INSTANCE;
    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = getModelWithCode(code);
        try {
            return largeLanguageModel.getService().request(request);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStreamRaw(request, fragmentDataHandler);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStreamRaw(request);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStream(request, chunkHandler);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    public LLMRegistration getServiceSpecificationRegistration() {
        return LLMRegistration.shared();
    }

    @Override
    public LargeLanguageModel getModelWithCode(String code) {
        return getServiceSpecificationRegistration().getModelWithCode(code);
    }

    @Override
    public void registerModel(LargeLanguageModel serviceSpecification) {
        getServiceSpecificationRegistration().registerModel(serviceSpecification);
    }

    @Override
    public Vertx getVertx() {
        return lateVertx.get();
    }

    public void setVertx(Vertx vertx) {
        System.out.println("LLMServiceFacade setVertx " + vertx);
        this.lateVertx.set(vertx);
    }

    @Override
    public WebClient getWebClient() {
        return lateWebClient.ensure(() -> {
            return WebClient.create(getVertx());
        });
    }

    @Override
    public HttpClient getHttpClient() {
        return lateHttpClient.ensure(() -> {
            HttpClientOptions httpClientOptions = new HttpClientOptions()
                    .setKeepAlive(true)
                    .setSsl(true)
                    .setDefaultPort(443);
            return getVertx().createHttpClient(httpClientOptions);
        });
    }

    @Override
    public Logger getLogger() {
        return lateLogger.ensure(() -> {
            return LoggerFactory.getShared().createLogger(LLMServiceFacade.class.getSimpleName());
        });
    }
}
