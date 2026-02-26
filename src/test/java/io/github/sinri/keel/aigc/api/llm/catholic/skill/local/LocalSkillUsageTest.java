package io.github.sinri.keel.aigc.api.llm.catholic.skill.local;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.AbstractSkillAgentUsageTest;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillProvider;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LocalSkillUsageTest extends AbstractSkillAgentUsageTest {
    public LocalSkillUsageTest() throws NotConfiguredException {
        super();
    }

    private String getSkillsDir() {
        String r = ConfigElement.root().readProperty("runtime_dir");
        Objects.requireNonNull(r, "The runtime directory is null!");
        return r + File.separator + "skills";
    }

    @Override
    protected SkillProvider buildSkillProvider() {
        return SkillProvider.withLocal(getVertx(), getSkillsDir());
    }

    @Test
    void test1(VertxTestContext testContext) {
        this.oneShot("现在这台设备，访问互联网上的服务器的时候所使用的 IP 地址是什么？")
            .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    @Timeout(value = 10,timeUnit = TimeUnit.MINUTES)
    void test2(VertxTestContext testContext) {
        this.oneShot("""
                     翻译下面的日语到英文
                     ----
                     日本からの転生者、カーマイン・マロット公爵令嬢は教会で洗礼を受ける。
                     洗礼によって彼女はギフトを授かった。
                     だが、洗礼の水晶に記された内容は彼女しか読めない日本語だった。
                     """)
            .onComplete(testContext.succeedingThenComplete());
    }
}
