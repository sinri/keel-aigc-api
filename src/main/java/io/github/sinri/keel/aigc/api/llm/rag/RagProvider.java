package io.github.sinri.keel.aigc.api.llm.rag;

import io.vertx.core.Future;

import java.util.List;

/**
 * RAG 机制的提供者，负责根据用户的原始请求提供参考资料。
 *
 * @param <I> 特定的运作指示类
 * @since 5.0.0
 */
public interface RagProvider<I extends RagInstruction, R extends RagReference> {
    /**
     * 根据用户的原始输入，提供一系列参考资料。
     *
     * @param originalPrompt 用户的原始输入
     * @return 包含 RAG 提供的参考资料的列表
     */
    Future<List<R>> getRagReferences(String originalPrompt);

    /**
     * 处理用户的输入，再包装成包含 RAG 提供的参考资料的最终提示词，用于生成 Request。
     *
     * @param originalPrompt 用户的原始输入
     * @return 包含 RAG 结果的最终提示词，用于生成 Request。
     */
    default Future<String> getRagEnrichedPrompt(String originalPrompt) {
        return getRagReferences(originalPrompt)
                .compose(references -> {
                    StringBuilder enrichedPrompt = new StringBuilder();
                    enrichedPrompt.append("----\n\n");
                    enrichedPrompt.append("# Context Referenced with RAG Provider:\n\n");
                    for (R reference : references) {
                        enrichedPrompt.append("## REF-").append(reference.index())
                                      .append(": ").append(reference.identity()).append("\n\n");
                        enrichedPrompt.append(reference.content()).append("\n\n");
                    }
                    enrichedPrompt.append("----\n\n");
                    enrichedPrompt.append(originalPrompt);
                    return Future.succeededFuture(enrichedPrompt.toString());
                });
    }

    I getInstruction();
}
