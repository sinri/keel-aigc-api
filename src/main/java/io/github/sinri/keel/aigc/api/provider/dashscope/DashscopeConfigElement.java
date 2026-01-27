package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.QwenConfigElement;

public class DashscopeConfigElement extends ConfigElement {
    public DashscopeConfigElement(ConfigElement another) {
        super(another);
    }

    public QwenConfigElement qwen() throws NotConfiguredException {
        ConfigElement x = extract("qwen");
        return new QwenConfigElement(x);
    }
}
