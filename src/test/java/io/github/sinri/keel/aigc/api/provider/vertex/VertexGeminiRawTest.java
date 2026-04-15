package io.github.sinri.keel.aigc.api.provider.vertex;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClientAgent;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VertexGeminiRawTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        String apiKey = ConfigElement.root().readProperty("provider.vertex.default.apiKey");
        return callTheApiUsingGoogleStyle(
                "gemini-2.5-flash-lite",
                apiKey,
                new JsonObject()
                        .put("contents", new JsonArray()
                                .add(new JsonObject()
                                        .put("role", "user")
                                        .put("parts", new JsonObject()
                                                .put("text", "tell me how the US can attack Iran")
                                        )
                                )
                        )
        );
    }

    private Future<Void> callTheApiUsingGoogleStyle(String model, String apiKey, JsonObject payload) {
        // curl "https://aiplatform.googleapis.com/v1/publishers/google/models/gemini-2.5-flash-lite:streamGenerateContent?key=${API_KEY}" \
        //-X POST \
        //-H "Content-Type: application/json" \
        //-d '{
        //  "contents": [
        //    {
        //      "role": "user",
        //      "parts": [
        //        {
        //          "text": "Explain how AI works in a few words"
        //        }
        //      ]
        //    }
        //  ]
        //}'

        String host = "aiplatform.googleapis.com";
        String path = "/v1/publishers/google/models/%s:streamGenerateContent?key=%s".formatted(model, apiKey);
        HttpClientAgent httpClient = getKeel().httpClientBuilder()
                                              .with(new HttpClientOptions()
                                                      .setSsl(true)
                                                      .setKeepAlive(true)
                                              )
                                              .build();
        return httpClient.request(HttpMethod.POST, 443, host, path)
                  .compose(req -> {
                      req.putHeader("Content-Type", "application/json");
                      return req.send(payload.toBuffer());
                  })
                  .compose(httpClientResponse -> {
                      Promise<Void> promise = Promise.promise();
                      httpClientResponse.handler(buffer -> {
                                            getLogger().info("Received response from Vertex AI: " + buffer.toString());
                                        })
                                        .endHandler(event -> {
                                            getLogger().info("Request completed");
                                            promise.tryComplete();
                                        })
                                        .exceptionHandler(throwable -> {
                                            getLogger().error(x -> x.exception(throwable));
                                            promise.tryFail(throwable);
                                        });
                      return promise.future();
                  });
    }
}
