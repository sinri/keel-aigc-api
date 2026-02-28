package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
import io.github.sinri.keel.aigc.api.llm.catholic.embedding.EmbeddingVector;
import io.github.sinri.keel.aigc.api.llm.catholic.embedding.TextEmbeddingGenerator;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过 Dashscope 的通用文本向量模型同步接口 API 来进行文本向量化。
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/text-embedding-synchronous-api">通用文本向量模型</a>
 */
public class DashscopeTextEmbeddingGenerator implements TextEmbeddingGenerator {
    public static final String MODEL_TEXT_EMBEDDING_V4 = "text-embedding-v4";
    private final String model;
    private final int dimension;
    private final String apiKey;
    private final Logger logger;
    private String textType = "document";
    private @Nullable String instruct;

    public DashscopeTextEmbeddingGenerator(String apiKey, String model, int dimension, Logger logger) {
        this.model = model;
        this.dimension = dimension;
        this.apiKey = apiKey;
        this.logger = logger;
    }

    /**
     * 文本转换为向量后可以应用于检索、聚类、分类等下游任务，对检索这类非对称任务为了达到更好的检索效果建议区分查询文本（query）和底库文本（document）类型，入库、聚类、分类等对称任务可以不用特殊指定，采用系统默认值document即可。
     *
     * @param textType {@code query} or {@code document}. 默认为 document。
     */
    public DashscopeTextEmbeddingGenerator setTextType(String textType) {
        this.textType = textType;
        return this;
    }

    public DashscopeTextEmbeddingGenerator setInstruct(@Nullable String instruct) {
        this.instruct = instruct;
        return this;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public Future<EmbeddingVector> generateEmbedding(String text) {
        JsonObject p = new JsonObject()
                .put("dimension", dimension)
                .put("output_type", "dense")
                .put("text_type", textType);
        if (instruct != null) {
            p.put("instruct", instruct);
        }
        JsonObject payload = new JsonObject()
                .put("model", model)
                .put("input", new JsonObject()
                        .put("texts", new JsonArray()
                                .add(text)
                        )
                )
                .put("parameters", p);
        logger.debug("Request Payload: \n" + payload.toString());
        return LLMServiceFacade.getWebClient()
                               .postAbs("https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding")
                               .bearerTokenAuthentication(apiKey)
                               .sendJsonObject(payload)
                               .compose(bufferHttpResponse -> {
                                   JsonObject body = bufferHttpResponse.bodyAsJsonObject();
                                   logger.debug("Response Body: \n" + body.toString());
                                   JsonArray jsonArray = body.getJsonObject("output")
                                                             .getJsonArray("embeddings");
                                   JsonObject jsonObject = jsonArray.getJsonObject(0);
                                   JsonArray embedding = jsonObject.getJsonArray("embedding");
                                   List<Double> v = new ArrayList<>();
                                   for (int i = 0; i < dimension; i++) {
                                       double d = embedding.getDouble(i);
                                       v.add(d);
                                   }
                                   return Future.succeededFuture(new EmbeddingVector(v));
                               });
    }
}
