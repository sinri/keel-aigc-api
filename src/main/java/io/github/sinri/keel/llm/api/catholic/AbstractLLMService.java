package io.github.sinri.keel.llm.api.catholic;

import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;

public abstract class AbstractLLMService implements LLMService {
    private final Logger logger;

    public AbstractLLMService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public final Logger getLogger() {
        return logger;
    }

    @Override
    public final WebClient getWebClient() {
        return LLMServiceFacade.getInstance().getWebClient();
    }

    @Override
    public final HttpClient getHttpClient() {
        return LLMServiceFacade.getInstance().getHttpClient();
    }

    @Override
    public final Vertx getVertx() {
        return LLMServiceFacade.getInstance().getVertx();
    }
}
