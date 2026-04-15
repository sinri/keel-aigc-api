package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.provider.azure.OpenAIChatCompletionsModelConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class Dalle3KitTest extends KeelJUnit5Test {
    @Test
    void test(VertxTestContext testContext) throws NotConfiguredException {
        OpenAIChatCompletionsModelConfigElement configElement = ProviderConfigElement.load().azure().openai().model("dalle3");
        WebClient webClient = WebClient.create(getVertx());
        Dalle3Kit dalle3Kit = new Dalle3Kit(configElement, webClient, getUnitTestLogger());
        dalle3Kit.draw(
                         p -> p.setPrompt("""
                                          そこには、これといって美人でも不美人でもない、平凡な少女が映り込んでいた。
                                          
                                          肌は十五という年齢にふさわしく滑らかだが、赤みに乏しく、どちらかといえばくすんで見える。
                                          目は眼鏡の存在に引っ張られて、何色なのかすら判別がつきづらく、薄めの唇は血の気がなくやや陰気である。
                                          衣服とて、清潔感はあるものの、サイズが合わないのかどこか野暮ったく、全体に冴えない印象が強い。
                                          """),
                         UUID.randomUUID().toString()
                 )
                 .compose(resp -> {
                     getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                     return Future.succeededFuture();
                 })
                 .onComplete(testContext.succeedingThenComplete());
    }

}