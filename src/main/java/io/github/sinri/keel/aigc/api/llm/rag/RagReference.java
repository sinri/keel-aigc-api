package io.github.sinri.keel.aigc.api.llm.rag;

/**
 * 通过 RAG 机制查到的参考资料，用于提供给大模型。
 * <p>
 * 本接口的实现期望为 record 类型的实体。
 *
 * @since 5.0.0
 */
public interface RagReference {
    /**
     * 获取到参考资料的排序。从 1 开始计。
     */
    int index();

    /**
     * 这份资料与查询目标的相合度评分。
     */
    double score();

    /**
     * 这份资料的身份，比如资料的 ID 之类的。
     */
    String identity();

    /**
     * 这份资料的内容。
     * <p>
     * 这个会直接丢给大模型观赏。
     */
    String content();

}
