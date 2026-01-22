package io.github.sinri.keel.llm.api.sect.provider.dashscope;

import io.github.sinri.keel.llm.api.catholic.AbstractLLMService;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.QwenUtils;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.request.QwenRequest;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.stream.QwenResponseBuffer;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync.QwenResponse;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class DashscopeMultimodalGenerationService extends AbstractLLMService {
    private final DashscopeProvider dashscopeProvider;

    public DashscopeMultimodalGenerationService(String apiKey, Logger logger) {
        super(logger);
        dashscopeProvider = new DashscopeProvider(apiKey, logger);
    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        QwenRequest qwenRequest = QwenUtils.toQwenRequest(request);
        return dashscopeProvider.requestMultimodalGeneration(
                                        getWebClient(),
                                        request.getModel(),
                                        qwenRequest.toJsonObject(),
                                        request.getRequestId()
                                )
                                .compose(jsonObject -> {
                                    QwenResponse qwenResponse = QwenResponse.wrap(jsonObject);
                                    MixChatResponse mixChatResponse = QwenUtils.from(qwenResponse);
                                    return Future.succeededFuture(mixChatResponse);
                                });
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        QwenRequest qwenRequest = QwenUtils.toQwenRequest(request);
        qwenRequest.parameters(p -> p.stream(true).incrementalOutput(true));

        return dashscopeProvider.requestMultimodalGenerationStream(
                getVertx(),
                getHttpClient(),
                request.getModel(),
                qwenRequest.toJsonObject(),
                s -> {
                    return Future.succeededFuture()
                                 .compose(v -> {
                                     return QwenUtils.parseStreamFragmentToChunk(s);
                                 })
                                 .compose(qwenResponseChunk -> {
                                     return fragmentDataHandler.apply(qwenResponseChunk.cloneAsJsonObject());
                                 })
                                 .onFailure(throwable -> {
                                     getLogger().error(x -> x
                                             .message("requestStream fragment process error")
                                             .exception(throwable));
                                 });
                },
                request.getTimeout(),
                request.getRequestId()
        );
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        QwenResponseBuffer qwenResponseBuffer = new QwenResponseBuffer();

        QwenRequest qwenRequest = QwenUtils.toQwenRequest(request);
        qwenRequest.parameters(p -> p.stream(true).incrementalOutput(true));
        return dashscopeProvider.requestMultimodalGenerationStream(
                                        getVertx(),
                                        getHttpClient(),
                                        request.getModel(),
                                        qwenRequest.toJsonObject(),
                                        s -> {
                                            getLogger().debug("qwenResponseFragment", x -> x.put("qwenResponseFragment", s));
                                            return Future.succeededFuture()
                                                         .compose(v -> {
                                                             return QwenUtils.parseStreamFragmentToChunk(s);
                                                         })
                                                         .compose(qwenResponseChunk -> {
                                                             getLogger().debug("qwenResponseChunk", x -> x.put("qwenResponseChunk", qwenResponseChunk.cloneAsJsonObject()));
                                                             qwenResponseBuffer.accept(qwenResponseChunk);
                                                             return Future.succeededFuture();
                                                         })
                                                         .onFailure(throwable -> {
                                                             getLogger().error(x -> x
                                                                     .message("requestStream fragment process error")
                                                                     .exception(throwable));
                                                         })
                                                         .compose(x -> Future.succeededFuture());
                                        },
                                        request.getTimeout(),
                                        request.getRequestId()
                                )
                                .compose(v -> {
                                    return Future.succeededFuture(qwenResponseBuffer.build());
                                })
                                .compose(qwenResponse -> {
                                    getLogger().debug("qwenResponse", x -> x.put("qwenResponse", qwenResponse.cloneAsJsonObject()));
                                    MixChatResponse mixChatResponse = QwenUtils.from(qwenResponse);
                                    return Future.succeededFuture(mixChatResponse);
                                });
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        QwenRequest qwenRequest = QwenUtils.toQwenRequest(request);
        qwenRequest.parameters(p -> p.stream(true).incrementalOutput(true));
        return dashscopeProvider.requestMultimodalGenerationStream(
                getVertx(),
                getHttpClient(),
                request.getModel(),
                qwenRequest.toJsonObject(),
                s -> Future.succeededFuture()
                           .compose(v -> QwenUtils.parseStreamFragmentToChunk(s))
                           .compose(qwenResponseChunk -> {
                               getLogger().debug("qwenResponseChunk", x -> x.put("qwenResponseChunk", qwenResponseChunk.cloneAsJsonObject()));
                               MixChatResponseChunk mixChatResponseChunk = QwenUtils.from(qwenResponseChunk);
                               getLogger().debug("mixChatResponseChunk", x -> x.put("mixChatResponseChunk", mixChatResponseChunk.cloneAsJsonObject()));
                               return chunkHandler.apply(mixChatResponseChunk);
                           })
                           .onFailure(throwable -> {
                               getLogger().error(x -> x
                                       .message("requestStream fragment process error")
                                       .exception(throwable));
                           }),
                request.getTimeout(),
                request.getRequestId()
        );
    }

}
