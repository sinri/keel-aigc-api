package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.LLMServiceFacadeBasedUnitTest;
import io.github.sinri.keel.aigc.api.llm.catholic.embedding.EmbeddingVector;
import io.github.sinri.keel.aigc.api.llm.catholic.embedding.VectorUtils;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.List;

class DashscopeTextEmbeddingGeneratorTest extends LLMServiceFacadeBasedUnitTest {
    public DashscopeTextEmbeddingGeneratorTest() throws NotConfiguredException {
        super();
    }
    @Test
    void test1(VertxTestContext testContext) throws NotConfiguredException {
        var apiKey = ProviderConfigElement.load().dashscope().qwen().apiKey();

        DashscopeTextEmbeddingGenerator generator = new DashscopeTextEmbeddingGenerator(
                apiKey,
                DashscopeTextEmbeddingGenerator.MODEL_TEXT_EMBEDDING_V4,
                1024,
                getUnitTestLogger()
        );

        Future.all(
                      generator.generateEmbedding("通用文本向量模型可将文本数据转换为数值向量，用于语义搜索、推荐、聚类、分类等下游任务。"),
                      generator.generateEmbedding("将文本处理成向量，然后向量之间进行比较，是 RAG 的一种手段。"),
                      generator.generateEmbedding("中国古代有很多人死于饥饿，而现在则几乎不再有此威胁。")
              )
              .compose(compositeFuture -> {
                  EmbeddingVector embedding1 = compositeFuture.resultAt(0);
                  EmbeddingVector embedding2 = compositeFuture.resultAt(1);
                  EmbeddingVector embedding3 = compositeFuture.resultAt(2);

                  getUnitTestLogger().info("embedding 1 dimension: " + embedding1.getDimension());
                  getUnitTestLogger().info("embedding 2 dimension: " + embedding2.getDimension());
                  getUnitTestLogger().info("embedding 3 dimension: " + embedding3.getDimension());

                  double cosineSimilarity_1_2 = VectorUtils.cosineSimilarity(embedding1, embedding2);
                  double cosineSimilarity_1_3 = VectorUtils.cosineSimilarity(embedding1, embedding3);
                  double cosineSimilarity_2_3 = VectorUtils.cosineSimilarity(embedding2, embedding3);
                  getUnitTestLogger().info("cosineSimilarity_1_2: " + cosineSimilarity_1_2);
                  getUnitTestLogger().info("cosineSimilarity_1_3: " + cosineSimilarity_1_3);
                  getUnitTestLogger().info("cosineSimilarity_2_3: " + cosineSimilarity_2_3);

                  return generator.setTextType("query")
                          .generateEmbedding("RAG工程里文本比较的实践方法有哪些")
                          .compose(embeddingVectorQ -> {
                              double cosineSimilarity_1_q = VectorUtils.cosineSimilarity(embedding1, embeddingVectorQ);
                              getUnitTestLogger().info("cosineSimilarity_1_q: " + cosineSimilarity_1_q);
                              double cosineSimilarity_2_q = VectorUtils.cosineSimilarity(embedding2, embeddingVectorQ);
                              getUnitTestLogger().info("cosineSimilarity_2_q: " + cosineSimilarity_2_q);
                              double cosineSimilarity_3_q = VectorUtils.cosineSimilarity(embedding3, embeddingVectorQ);
                              getUnitTestLogger().info("cosineSimilarity_3_q: " + cosineSimilarity_3_q);

                              return Future.succeededFuture();
                          });
              })
              .onComplete(testContext.succeedingThenComplete());
    }
}