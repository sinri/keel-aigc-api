package io.github.sinri.keel.aigc.api.llm.catholic.embedding;

import io.vertx.core.Future;

import java.math.BigDecimal;
import java.util.List;

public interface TextEmbeddingGenerator {
    int dimension();

    Future<EmbeddingVector> generateEmbedding(String text);
}
