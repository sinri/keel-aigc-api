package io.github.sinri.keel.aigc.api.llm.catholic.embedding;

import java.util.List;

/**
 * 一个用于计算 N 维向量之间相似度和距离的工具类。
 * 特别适用于 RAG (Retrieval-Augmented Generation) 场景。
 */
public class VectorUtils {

    private VectorUtils() {
        // 工具类，防止实例化
    }

    // --- double[] 具体实现 ---

    /**
     * 计算两个向量的点积 (Dot Product)。
     * <p>
     * 以 double[] 为入参的具体实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的点积
     * @throws IllegalArgumentException 如果向量维度不匹配或为 null
     */
    public static double dotProduct(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        double sum = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            sum += vectorA[i] * vectorB[i];
        }
        return sum;
    }

    /**
     * 计算向量的模长 (Magnitude / Euclidean Norm)。
     * <p>
     * 以 double[] 为入参的具体实现。
     *
     * @param vector 输入向量
     * @return 向量的模长
     * @throws IllegalArgumentException 如果向量为 null
     */
    public static double magnitude(double[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null.");
        }
        return Math.sqrt(dotProduct(vector, vector));
    }

    /**
     * 计算两个向量之间的欧几里得距离 (Euclidean Distance)。
     * <p>
     * 以 double[] 为入参的具体实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量之间的欧几里得距离
     * @throws IllegalArgumentException 如果向量维度不匹配或为 null
     */
    public static double euclideanDistance(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        double sumOfSquaredDiffs = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            double diff = vectorA[i] - vectorB[i];
            sumOfSquaredDiffs += diff * diff;
        }
        return Math.sqrt(sumOfSquaredDiffs);
    }

    /**
     * 计算两个向量之间的余弦相似度 (Cosine Similarity)。
     * <p>
     * 结果范围在 [-1, 1] 之间。1 表示方向完全相同，-1 表示方向完全相反，0 表示正交。
     * <p>
     * 以 double[] 为入参的具体实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的余弦相似度
     * @throws IllegalArgumentException 如果向量维度不匹配、为 null 或任一向量为零向量
     */
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        double dot = dotProduct(vectorA, vectorB);
        double magnitudeA = magnitude(vectorA);
        double magnitudeB = magnitude(vectorB);
        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            throw new IllegalArgumentException("Cannot calculate cosine similarity with a zero vector.");
        }
        return dot / (magnitudeA * magnitudeB);
    }

    // --- EmbeddingVector 委托 ---

    /**
     * 计算两个向量的点积 (Dot Product)。
     * <p>
     * 入参为 EmbeddingVector，委托至 double[] 实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的点积
     * @throws IllegalArgumentException 如果向量为 null 或维度不匹配
     */
    public static double dotProduct(EmbeddingVector vectorA, EmbeddingVector vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return dotProduct(vectorA.asArray(), vectorB.asArray());
    }

    /**
     * 计算向量的模长 (Magnitude / Euclidean Norm)。
     * <p>
     * 入参为 EmbeddingVector，委托至 double[] 实现。
     *
     * @param vector 输入向量
     * @return 向量的模长
     * @throws IllegalArgumentException 如果向量为 null
     */
    public static double magnitude(EmbeddingVector vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null.");
        }
        return magnitude(vector.asArray());
    }

    /**
     * 计算两个向量之间的欧几里得距离 (Euclidean Distance)。
     * <p>
     * 入参为 EmbeddingVector，委托至 double[] 实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量之间的欧几里得距离
     * @throws IllegalArgumentException 如果向量为 null 或维度不匹配
     */
    public static double euclideanDistance(EmbeddingVector vectorA, EmbeddingVector vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return euclideanDistance(vectorA.asArray(), vectorB.asArray());
    }

    /**
     * 计算两个向量之间的余弦相似度 (Cosine Similarity)。
     * <p>
     * 入参为 EmbeddingVector，委托至 double[] 实现。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的余弦相似度
     * @throws IllegalArgumentException 如果向量为 null、维度不匹配或任一为零向量
     */
    public static double cosineSimilarity(EmbeddingVector vectorA, EmbeddingVector vectorB) {
        if (vectorA == null || vectorB == null) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return cosineSimilarity(vectorA.asArray(), vectorB.asArray());
    }

    // --- List 委托（向后兼容）---

    /**
     * 计算两个向量的点积 (Dot Product)。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的点积
     * @throws IllegalArgumentException 如果向量维度不匹配或为 null
     */
    public static double dotProduct(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return dotProduct(vectorA.stream().mapToDouble(Double::doubleValue).toArray(),
                vectorB.stream().mapToDouble(Double::doubleValue).toArray());
    }

    /**
     * 计算向量的模长 (Magnitude / Euclidean Norm)。
     *
     * @param vector 输入向量
     * @return 向量的模长
     * @throws IllegalArgumentException 如果向量为 null
     */
    public static double magnitude(List<Double> vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null.");
        }
        return magnitude(vector.stream().mapToDouble(Double::doubleValue).toArray());
    }

    /**
     * 计算两个向量之间的欧几里得距离 (Euclidean Distance)。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量之间的欧几里得距离
     * @throws IllegalArgumentException 如果向量维度不匹配或为 null
     */
    public static double euclideanDistance(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return euclideanDistance(vectorA.stream().mapToDouble(Double::doubleValue).toArray(),
                vectorB.stream().mapToDouble(Double::doubleValue).toArray());
    }

    /**
     * 计算两个向量之间的余弦相似度 (Cosine Similarity)。
     * 结果范围在 [-1, 1] 之间。1 表示方向完全相同，-1 表示方向完全相反，0 表示正交。
     *
     * @param vectorA 第一个向量
     * @param vectorB 第二个向量
     * @return 两个向量的余弦相似度
     * @throws IllegalArgumentException 如果向量维度不匹配、为 null 或任一向量为零向量
     */
    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimensionality.");
        }
        return cosineSimilarity(vectorA.stream().mapToDouble(Double::doubleValue).toArray(),
                vectorB.stream().mapToDouble(Double::doubleValue).toArray());
    }

    // --- 示例和测试 ---
//    public static void main(String[] args) {
//        // 创建两个示例向量
//        List<Double> vec1 = Arrays.asList(3.5, 2.1, 4.0);
//        List<Double> vec2 = Arrays.asList(2.0, 3.0, 1.5);
//        List<Double> vec3 = Arrays.asList(7.0, 4.2, 8.0); // vec1 的两倍，方向相同
//
//        System.out.println("--- Vector Comparison Example ---");
//        System.out.printf("Vector 1: %s%n", vec1);
//        System.out.printf("Vector 2: %s%n", vec2);
//        System.out.printf("Vector 3 (2x Vec1): %s%n%n", vec3);
//
//        try {
//            // 计算并打印余弦相似度
//            System.out.printf("Cosine Similarity (Vec1 & Vec2): %.4f%n", cosineSimilarity(vec1, vec2));
//            System.out.printf("Cosine Similarity (Vec1 & Vec3): %.4f%n", cosineSimilarity(vec1, vec3));
//
//            // 计算并打印欧几里得距离
//            System.out.printf("Euclidean Distance (Vec1 & Vec2): %.4f%n", euclideanDistance(vec1, vec2));
//            System.out.printf("Euclidean Distance (Vec1 & Vec3): %.4f%n", euclideanDistance(vec1, vec3));
//
//            // 计算并打印点积
//            System.out.printf("Dot Product (Vec1 & Vec2): %.4f%n", dotProduct(vec1, vec2));
//            System.out.printf("Dot Product (Vec1 & Vec3): %.4f%n", dotProduct(vec1, vec3));
//
//        } catch (IllegalArgumentException e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }
}