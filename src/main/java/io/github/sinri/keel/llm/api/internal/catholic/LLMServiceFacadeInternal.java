package io.github.sinri.keel.llm.api.internal.catholic;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.core.utils.value.ValueBox;
import io.github.sinri.keel.llm.api.catholic.LLMService;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
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

public class LLMServiceFacadeInternal implements LLMService {
    private final ValueBox<Vertx> lateVertx = new ValueBox<>();
    private final ValueBox<WebClient> lateWebClient = new ValueBox<>();
    private final ValueBox<HttpClient> lateHttpClient = new ValueBox<>();
    private final LateObject<Logger> lateLogger = new LateObject<>();

    public LLMServiceFacadeInternal() {

    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = LLMRegistration.shared().getModelWithCode(code);
        try {
            return largeLanguageModel.getService().request(request);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = LLMRegistration.shared().getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStreamRaw(request, fragmentDataHandler);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = LLMRegistration.shared().getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStreamRaw(request);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        String code = request.getModel();
        LargeLanguageModel largeLanguageModel = LLMRegistration.shared().getModelWithCode(code);
        try {
            return largeLanguageModel.getService().requestStream(request, chunkHandler);
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
    }


    @Override
    public Vertx getVertx() {
        return lateVertx.getNonNullValue();
    }

    public void setVertx(Vertx vertx) {
        // System.out.println("LLMServiceFacade setVertx " + vertx);
        this.lateVertx.setValue(vertx);
        this.lateWebClient.clear();
        this.lateHttpClient.clear();
    }

    @Override
    public WebClient getWebClient() {
        return lateWebClient.ensureNonNullValue(() -> {
            return new ValueBox.EnsuredValueWithExpire<>(WebClient.create(getVertx()), 0);
        });
    }

    @Override
    public HttpClient getHttpClient() {
        return lateHttpClient.ensureNonNullValue(() -> {
            return new ValueBox.EnsuredValueWithExpire<>(
                    getVertx().createHttpClient(new HttpClientOptions()
                            .setKeepAlive(true)
                            .setSsl(true)
                            .setDefaultPort(443)
                    ),
                    0
            );
        });
    }

    @Override
    public Logger getLogger() {
        return lateLogger.ensure(() -> {
            return LoggerFactory.getShared().createLogger(LLMServiceFacade.class.getSimpleName());
        });
    }
}
