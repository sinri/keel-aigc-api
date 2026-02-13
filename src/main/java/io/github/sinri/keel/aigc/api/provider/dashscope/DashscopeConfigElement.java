package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.QwenConfigElement;

public class DashscopeConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "dashscope";
    public DashscopeConfigElement(ConfigElement another) {
        super(another);
    }

    public QwenConfigElement qwen() throws NotConfiguredException {
        ConfigElement x = extract(QwenConfigElement.CONFIG_ELEMENT_NAME);
        return new QwenConfigElement(x);
    }

    public static DashscopeConfigElement create(QwenConfigElement qwen) {
        ConfigElement configElement = new ConfigElement(CONFIG_ELEMENT_NAME);
        configElement.addChild(qwen);
        return new DashscopeConfigElement(configElement);
    }
}
