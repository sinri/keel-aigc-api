package io.github.sinri.keel.llm.api.sect.provider.volces;

import io.github.sinri.keel.llm.api.catholic.AbstractLLMService;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.DoubaoUtils;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream.DoubaoResponseBuffer;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.sync.DoubaoResponse;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class VolcesTextGenerationService extends AbstractLLMService {
    private final VolcesProvider provider;

    public VolcesTextGenerationService(VolcesConfigElement configElement, Logger logger) {
        super(logger);
        this.provider = new VolcesProvider(configElement, logger);
    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        return provider.request(
                               getWebClient(),
                               request.getModel(),
                               DoubaoUtils.toDoubaoRequest(request).toJsonObject(),
                               request.getRequestId()
                       )
                       .compose(j -> {
                           DoubaoResponse doubaoResponse = DoubaoResponse.wrap(j);
                           MixChatResponse mixChatResponse = DoubaoUtils.from(doubaoResponse);
                           return Future.succeededFuture(mixChatResponse);
                       });
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        request.setStream(true);
        return provider.requestStream(
                getKeel(),
                getHttpClient(),
                request.getModel(),
                DoubaoUtils.toDoubaoRequest(request).toJsonObject(),
                fragment -> {
                    return DoubaoUtils.parseStreamFragmentToChunk(fragment)
                                      .compose(doubaoResponseChunk -> {
                                          return fragmentDataHandler.apply(doubaoResponseChunk.cloneAsJsonObject());
                                      });
                },
                request.getTimeout(),
                request.getRequestId()
        );
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        DoubaoResponseBuffer buffer = new DoubaoResponseBuffer();

        request.setStream(true);
        return provider.requestStream(
                               getKeel(),
                               getHttpClient(),
                               request.getModel(),
                               DoubaoUtils.toDoubaoRequest(request).toJsonObject(),
                               fragment -> {
                                   return Future.succeededFuture()
                                                .compose(v -> {
                                                    return DoubaoUtils.parseStreamFragmentToChunk(fragment);
                                                })
                                                .compose(doubaoResponseChunk -> {
                                                    getLogger().debug(
                                                            "requestStreamRaw handle fragment",
                                                            x -> x
                                                                    .put("fragment", fragment)
                                                                    .put("doubaoResponseChunk", doubaoResponseChunk.cloneAsJsonObject())
                                                    );
                                                    buffer.accept(doubaoResponseChunk);
                                                    return Future.succeededFuture();
                                                })
                                                .onFailure(throwable -> {
                                                    getLogger().debug(x -> x
                                                            .message("requestStream fragment process error")
                                                            .exception(throwable));
                                                })
                                                .compose(v -> Future.succeededFuture());
                               },
                               request.getTimeout(),
                               request.getRequestId()
                       )
                       .compose(v -> {
                           return Future.succeededFuture(DoubaoUtils.from(buffer.build()));
                       });
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        request.setStream(true);
        return provider.requestStream(
                getKeel(),
                getHttpClient(),
                request.getModel(),
                DoubaoUtils.toDoubaoRequest(request).toJsonObject(),
                fragment -> {
                    return DoubaoUtils.parseStreamFragmentToChunk(fragment)
                                      .compose(doubaoResponseChunk -> {
                                          MixChatResponseChunk mixChatResponseChunk = DoubaoUtils.from(doubaoResponseChunk);
                                          return chunkHandler.apply(mixChatResponseChunk);
                                      });
                },
                request.getTimeout(),
                request.getRequestId()
        );
    }
}
