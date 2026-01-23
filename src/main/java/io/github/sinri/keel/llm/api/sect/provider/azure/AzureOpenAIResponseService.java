package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.llm.api.catholic.AbstractLLMService;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.OpenAIStatefulUtils;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatRequest;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatResponse;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

@TechnicalPreview
public class AzureOpenAIResponseService extends AbstractLLMService {
    private final AzureOpenAIResponseProvider provider;

    public AzureOpenAIResponseService(OpenAIConfigElement openAIConfigElement, Logger logger) {
        super(logger);
        this.provider = new AzureOpenAIResponseProvider(openAIConfigElement, logger);
    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        StatefulChatRequest statefulChatRequest = OpenAIStatefulUtils.toStatefulChatRequest(request);
        return provider.request(
                               getWebClient(),
                               request.getModel(),
                               statefulChatRequest.toJsonObject(),
                               request.getRequestId()
                       )
                       .compose(j -> {
                           StatefulChatResponse resp = StatefulChatResponse.wrap(j);
                           MixChatResponse mixChatResponse = OpenAIStatefulUtils.from(resp);
                           return Future.succeededFuture(mixChatResponse);
                       });
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
