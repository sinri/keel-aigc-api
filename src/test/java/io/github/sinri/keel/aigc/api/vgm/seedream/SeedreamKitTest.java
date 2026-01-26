package io.github.sinri.keel.aigc.api.vgm.seedream;

import io.github.sinri.keel.aigc.api.llm.sect.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.provider.volces.VolcesConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SeedreamKitTest extends KeelJUnit5Test {
    @Test
    void testSync(VertxTestContext testContext) throws NotConfiguredException {
        VolcesConfigElement configElement = ProviderConfigElement.load().volces();
        //        String s = configElement.apiKey();
        //        String model = configElement.model("seedream-4d0");
        SeedreamKit seedreamKit = new SeedreamKit(configElement);
        WebClient webClient = WebClient.create(getVertx());
        seedreamKit.seedream4(
                           webClient,
                           "seedream-4d0",
                           req -> req
                                   .setPrompt("天上一只鸟，地上一只猫，河里一条鱼")
                                   .setResponseFormat("url"),
                           UUID.randomUUID().toString()
                   )
                   .compose(resp -> {
                       getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                       resp.getData().forEach(datum -> {
                           String url = datum.getUrl();
                           getUnitTestLogger().info("url", x -> x.put("url", url));
                       });
                       return Future.succeededFuture();
                   })
                   .onComplete(testContext.succeedingThenComplete());
    }
}