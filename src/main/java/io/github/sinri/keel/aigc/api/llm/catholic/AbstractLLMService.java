package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;

/**
 * LLM 服务的抽象基类。
 * <p>
 * 提供日志记录器、Web 客户端、HTTP 客户端和 Keel 实例的默认实现。
 *
 * @since 5.0.0
 */
public abstract class AbstractLLMService implements LLMService {
    private final Logger logger;

    /**
     * 构造函数。
     *
     * @param logger 日志记录器
     */
    public AbstractLLMService(Logger logger) {
        this.logger = logger;
    }

    /**
     * 获取日志记录器。
     *
     * @return 日志记录器实例
     */
    @Override
    public final Logger getLogger() {
        return logger;
    }

    /**
     * 获取 Web 客户端。
     *
     * @return Web 客户端实例
     */
    @Override
    public final WebClient getWebClient() {
        return LLMServiceFacade.getWebClient();
    }

    /**
     * 获取 HTTP 客户端。
     *
     * @return HTTP 客户端实例
     */
    @Override
    public final HttpClient getHttpClient() {
        return LLMServiceFacade.getHttpClient();
    }

    /**
     * 获取 Keel 实例。
     *
     * @return Keel 实例
     */
    @Override
    public Keel getKeel() {
        return LLMServiceFacade.getKeel();
    }
}
