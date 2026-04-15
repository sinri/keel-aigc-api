package io.github.sinri.keel.aigc.api.provider.vertex;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sinri.keel.aigc.api.provider.LLMProvider;
import io.github.sinri.keel.aigc.api.provider.WritableInputStream;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.io.InputStream;
import java.util.function.Function;

public class VertexGeminiProvider implements LLMProvider {
    private final Logger logger;
    private final VertexProjectConfigElement vertexProjectConfigElement;

    public VertexGeminiProvider(VertexProjectConfigElement vertexProjectConfigElement, Logger logger) {
        this.logger = logger;
        this.vertexProjectConfigElement = vertexProjectConfigElement;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        String host = "aiplatform.googleapis.com";
        String path = null;
        try {
            path = "/v1/publishers/google/models/%s:generateContent?key=%s".formatted(chatModel, vertexProjectConfigElement.apiKey());
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        getLogger().info("uri: " + path);
        return webClient.postAbs("https://" + host + path)
                        .putHeader("Content-Type", "application/json")
                        .sendJsonObject(requestPayload)
                        .compose(bufferHttpResponse -> {
                            JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                            return Future.succeededFuture(jsonObject);
                        });
    }

    @Override
    public Future<Void> requestStream(Vertx vertx, HttpClient httpClient, String chatModel, JsonObject requestPayload, Function<String, Future<Void>> cutterProcessFunc, long cutterTimeout, String requestId) {
        String host = "aiplatform.googleapis.com";
        String path = null;
        try {
            path = "/v1/publishers/google/models/%s:streamGenerateContent?key=%s".formatted(chatModel, vertexProjectConfigElement.apiKey());
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        getLogger().info("uri: " + path);
        return httpClient.request(HttpMethod.POST, 443, host, path)
                         .compose(req -> {
                             req.putHeader("Content-Type", "application/json");
                             return req.send(requestPayload.toString());
                         })
                         .compose(httpClientResponse -> {
                             getLogger().info("status code: " + httpClientResponse.statusCode());
                             WritableInputStream writableInputStream = new WritableInputStream();
                             IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(cutterProcessFunc::apply, cutterTimeout);

                             httpClientResponse.handler(buffer -> {
                                                   // getLogger().info("1. get buffer", ctx -> ctx.put("string", buffer.toString(StandardCharsets.UTF_8)));
                                                   writableInputStream.accept(buffer);
                                               })
                                               .endHandler(event -> {
                                                   writableInputStream.end();
                                               })
                                               .exceptionHandler(throwable -> {
                                                   getLogger().error(x -> x.exception(throwable));
                                                   writableInputStream.end();
                                               });
                             return cutter.deployMe(vertx, new DeploymentOptions())
                                          .compose(v -> {
                                              //writableInputStream.accept(Buffer.buffer("[{}"));
                                              return handleResponseStream(vertx, writableInputStream, cutter);
                                          });
                         });
    }

    private Future<Void> handleResponseStream(Vertx vertx, InputStream inputStream, IntravenouslyCutterOnString cutter) {
        Thread thread = new Thread(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                // 创建解析器
                try (var parser = mapper.getFactory().createParser(inputStream)) {
                    // 移动到数组的开始符号 '['
                    if (parser.nextToken() != JsonToken.START_ARRAY) {
                        throw new IllegalStateException("Expected content to be an array");
                    }

                    // 读取数组中的每个元素
                    JsonToken token = parser.nextToken();
                    while (token != JsonToken.END_ARRAY && token != null) {
                        if (token == JsonToken.START_OBJECT) {
                            // 读取为 Jackson 的 JsonNode，然后转换为 Vertx 的 JsonObject
                            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(parser);
                            JsonObject obj = new JsonObject(mapper.writeValueAsString(node));
                            // getLogger().info("2. OBJECT FOUND: " + obj.toString());
                            cutter.acceptFromStream(obj.toBuffer());
                            cutter.acceptFromStream(Buffer.buffer("\n\n"));
                        } else if (token == JsonToken.VALUE_NULL) {
                            parser.skipChildren();
                        }
                        token = parser.nextToken();
                        // getLogger().info("3. next token: " + token.toString());
                    }
                }
                cutter.stopHere();
            } catch (Exception e) {
                cutter.stopHere(e);
            }
        });

        thread.start();
        return cutter.waitForAllHandled()
                     .onComplete(ar -> {
                         try {
                             thread.join();
                         } catch (InterruptedException e) {
                             throw new RuntimeException(e);
                         }
                         cutter.undeployMe();
                     });
    }
}
