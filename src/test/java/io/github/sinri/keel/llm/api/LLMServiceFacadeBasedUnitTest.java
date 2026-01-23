package io.github.sinri.keel.llm.api;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeAll;

public abstract class LLMServiceFacadeBasedUnitTest extends KeelJUnit5Test {
    public LLMServiceFacadeBasedUnitTest() throws NotConfiguredException {
        LLMServiceFacade.registerLLMsFollowingConfig();
    }

    @BeforeAll
    public static void beforeAll() {
        LLMServiceFacade.setVertx(rtoc.vertx());
    }

    protected WebClient getWebClientForLLM() {
        return LLMServiceFacade.getWebClient();
    }

    protected Vertx getVertxForLLM() {
        return LLMServiceFacade.getVertx();
    }

    protected HttpClient getHttpClientForLLM() {
        return LLMServiceFacade.getHttpClient();
    }
}
