package io.github.sinri.keel.llm.api.sect;

/**
 * 表示请求被内容过滤器拦截的异常。
 * 当请求内容包含不当内容（如暴力、色情、仇恨言论等）时抛出此异常。
 * 
 * @since 2.0.0
 */
public class FilteredRequest extends RuntimeException {
    public FilteredRequest() {
        super("该请求犯了天条已被过滤。");
    }
}
