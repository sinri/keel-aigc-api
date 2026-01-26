package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.llm.catholic.AbstractLLMService;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.aigc.api.llm.sect.FilteredRequest;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.OpenAIClassicUtils;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIPromptFilterResults;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseBuffer;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync.GPTResponse;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

public class AzureOpenAITextGenerationService extends AbstractLLMService {
    private final AzureOpenAIClassicProvider provider;

    public AzureOpenAITextGenerationService(OpenAIConfigElement openAIConfigElement, Logger logger) {
        super(logger);
        this.provider = new AzureOpenAIClassicProvider(openAIConfigElement, logger);
    }

    @Override
    public Future<MixChatResponse> request(MixChatRequest request) {
        JsonObject requestPayload = OpenAIClassicUtils.toGPTRequest(request).toJsonObject();
        return provider.request(
                               getWebClient(),
                               request.getModel(),
                               requestPayload,
                               request.getRequestId()
                       )
                       .compose(j -> {
                           GPTResponse gptResponse = GPTResponse.wrap(j);
                           List<OpenAIPromptFilterResults> promptFilterResults = gptResponse.getPromptFilterResults();

                           if (promptFilterResults.stream()
                                                  .noneMatch(pfr -> {
                                                      var cfr = pfr.getContentFilterResults();
                                                      if (cfr == null) return false;
                                                      return cfr.whetherFiltered();
                                                  })
                           ) {
                               return Future.succeededFuture(gptResponse);
                           } else {
                               throw new FilteredRequest();
                           }
                       })
                       .compose(gptResponse -> {
                           MixChatResponse mixChatResponse = OpenAIClassicUtils.from(gptResponse);
                           return Future.succeededFuture(mixChatResponse);
                       });
    }

    @Override
    public Future<Void> requestStreamRaw(MixChatRequest request, Function<JsonObject, Future<Void>> fragmentDataHandler) {
        JsonObject requestPayload = OpenAIClassicUtils.toGPTRequest(request)
                                                      .stream(true)
                                                      .toJsonObject();
        return provider.requestStream(
                getKeel(),
                getHttpClient(),
                request.getModel(),
                requestPayload,
                fragment -> {
                    return OpenAIClassicUtils.parseStreamFragmentToChunk(fragment)
                                             .compose(chunk -> {
                                          return fragmentDataHandler.apply(chunk.cloneAsJsonObject());
                                      }, throwable -> {
                                          getLogger().debug(log -> log
                                                  .exception(throwable)
                                                  .message("io.github.sinri.keel.llm.api.sect.provider.azure.AzureOpenAITextGenerationService.requestStreamRaw(io.github.sinri.keel.llm.api.catholic.request.MixChatRequest, java.util.function.Function<io.vertx.core.json.JsonObject,io.vertx.core.Future<java.lang.Void>>) failed"));
                                          return Future.succeededFuture();
                                      });
                },
                request.getTimeout(),
                request.getRequestId()
        );
    }

    @Override
    public Future<MixChatResponse> requestStreamRaw(MixChatRequest request) {
        GPTResponseBuffer buffer = new GPTResponseBuffer();

        JsonObject requestPayload = OpenAIClassicUtils.toGPTRequest(request)
                                                      .stream(true)
                                                      .toJsonObject();
        return provider.requestStream(
                               getKeel(),
                               getHttpClient(),
                               request.getModel(),
                               requestPayload,
                               fragment -> {
                                   return OpenAIClassicUtils.parseStreamFragmentToChunk(fragment)
                                                            .compose(chunk -> {
                                                         buffer.accept(chunk);
                                                         return Future.succeededFuture();
                                                     }, throwable -> {
                                                         getLogger().debug(log -> log
                                                                 .exception(throwable)
                                                                 .message("io.github.sinri.keel.llm.api.sect.provider.azure.AzureOpenAITextGenerationService.requestStreamRaw(io.github.sinri.keel.llm.api.catholic.request.MixChatRequest, java.util.function.Function<io.vertx.core.json.JsonObject,io.vertx.core.Future<java.lang.Void>>) failed"));
                                                         return Future.succeededFuture();
                                                     });
                               },
                               request.getTimeout(),
                               request.getRequestId()
                       )
                       .compose(v -> {
                           return Future.succeededFuture(buffer.build());
                       })
                       .compose(gptResponse -> {
                           return Future.succeededFuture(OpenAIClassicUtils.from(gptResponse));
                       });
    }

    @Override
    public Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler) {
        JsonObject requestPayload = OpenAIClassicUtils.toGPTRequest(request)
                                                      .stream(true)
                                                      .toJsonObject();
        return provider.requestStream(
                getKeel(),
                getHttpClient(),
                request.getModel(),
                requestPayload,
                fragment -> {
                    return OpenAIClassicUtils.parseStreamFragmentToChunk(fragment)
                                             .compose(chunk -> {
                                          MixChatResponseChunk mixChatResponseChunk = OpenAIClassicUtils.from(chunk);
                                          return chunkHandler.apply(mixChatResponseChunk);
                                      }, throwable -> {
                                          getLogger().debug(log -> log
                                                  .exception(throwable)
                                                  .message("io.github.sinri.keel.llm.api.sect.provider.azure.AzureOpenAITextGenerationService.requestStreamRaw(io.github.sinri.keel.llm.api.catholic.request.MixChatRequest, java.util.function.Function<io.vertx.core.json.JsonObject,io.vertx.core.Future<java.lang.Void>>) failed"));
                                          return Future.succeededFuture();
                                      });
                },
                request.getTimeout(),
                request.getRequestId()
        );
    }
}
