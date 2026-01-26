package io.github.sinri.keel.aigc.api.llm.catholic.response.stream;

/**
 * 流式报文数据碎片聚合器。
 *
 * @param <P> 流式报文数据碎片类型
 * @param <E> 将碎片聚合得到的完整实体类型
 */
public interface StreamPieceCollector<P, E> {
    /**
     * 接受一个数据碎片。
     *
     * @param piece 数据碎片
     */
    void accept(P piece);

    /**
     * 构建一个完整实体。
     *
     * @return 完整实体
     */
    E build();
}
