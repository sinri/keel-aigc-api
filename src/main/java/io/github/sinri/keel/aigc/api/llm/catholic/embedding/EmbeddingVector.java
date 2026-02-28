package io.github.sinri.keel.aigc.api.llm.catholic.embedding;

import io.vertx.core.json.JsonArray;

import java.util.Arrays;
import java.util.List;

/**
 * 文本等内容经过 Embedding 处理后得到的特定维度的向量。
 * @since 5.0.0
 */
public class EmbeddingVector {
    private final double[] vector;
    private final int dimension;

    public EmbeddingVector(double[] vector) {
        this.vector = vector;
        this.dimension = vector.length;
    }

    public EmbeddingVector(List<Double> vector) {
        this(vector.stream().mapToDouble(Double::doubleValue).toArray());
    }

    public double[] asArray() {
        return vector;
    }

    public List<Double> asList() {
        return Arrays.stream(vector).boxed().toList();
    }

    public JsonArray asJsonArray() {
        return new JsonArray(asList());
    }

    public int getDimension() {
        return dimension;
    }
}
