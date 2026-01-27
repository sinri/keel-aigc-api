package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.aigc.api.llm.sect.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class GPTImageKitTest extends KeelJUnit5Test {

    @Test
    @Timeout(180_000)
    void testGenerate(VertxTestContext testContext) throws NotConfiguredException {
        String runtimeDir = ConfigElement.root().readProperty("runtime_dir");
        Objects.requireNonNull(runtimeDir);

        OpenAIModelConfigElement configElement = ProviderConfigElement.load().azure().openai().model("gpt-image-1");
        WebClient webClient = WebClient.create(getVertx());
        GPTImageKit gptImageKit = new GPTImageKit(configElement, webClient);
        gptImageKit.generateImage(new GenerateImageRequest()
                           .setPrompt(
                                   """
                                   鬱蒼とした大木が立ち並び、見える範囲では終わりが見えない暗い森。
                                   暗くて、下草が生えていないので、結構な範囲まで見渡せる。
                                   上を見ると空が見えて、そこだけ陽の光が差し込んでいる。
                                   空は青空だ。
                                   """
                           )
                           .setOutputFormat(OutputFormat.png)
                   )
                   .compose(resp -> {
                       getUnitTestLogger().info("resp", x -> x.put("resp", resp));
                       List<GptImageResultDatum> data = resp.getData();

                       for(int i = 0; i < Objects.requireNonNull(data).size(); i++){
                           Buffer buffer = data.get(i).transformToBuffer();
                           String fn=runtimeDir+"/gpt-image-gen-"+i+".png";
                           getKeel().fileSystem().writeFileBlocking(fn, buffer);
                           getUnitTestLogger().info("generated image", x -> x.put("fn", fn));
                       }
                       return Future.succeededFuture();
                   })
                   .onComplete(testContext.succeedingThenComplete());
    }
}