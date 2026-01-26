package io.github.sinri.keel.aigc.api.llm;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMServiceFacade;
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
        LLMServiceFacade.setKeel(rtoc.vertx());
    }

    protected WebClient getWebClientForLLM() {
        return LLMServiceFacade.getWebClient();
    }

    @Deprecated
    protected Vertx getVertxForLLM() {
        return LLMServiceFacade.getKeel();
    }

    protected Keel getKeelForLLM() {
        return LLMServiceFacade.getKeel();
    }

    protected HttpClient getHttpClientForLLM() {
        return LLMServiceFacade.getHttpClient();
    }
}
