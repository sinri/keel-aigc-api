package io.github.sinri.keel.aigc.api.provider;

import io.github.sinri.keel.aigc.api.provider.azure.AzureConfigElement;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeConfigElement;
import io.github.sinri.keel.aigc.api.provider.volces.VolcesConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

/**
 * Load AIGC Provider Configuration from {@link io.github.sinri.keel.base.configuration.ConfigElement#root()}.
 */
public class ProviderConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "provider";

    public ProviderConfigElement(ConfigElement another) {
        super(another);
    }

    public static ProviderConfigElement load() throws NotConfiguredException {
        ConfigElement x = ConfigElement.root().extract(CONFIG_ELEMENT_NAME);
        return new ProviderConfigElement(x);
    }

    public DashscopeConfigElement dashscope() throws NotConfiguredException {
        ConfigElement x = extract(DashscopeConfigElement.CONFIG_ELEMENT_NAME);
        return new DashscopeConfigElement(x);
    }

    public AzureConfigElement azure() throws NotConfiguredException {
        ConfigElement x = extract(AzureConfigElement.CONFIG_ELEMENT_NAME);
        return new AzureConfigElement(x);
    }

    public VolcesConfigElement volces() throws NotConfiguredException {
        ConfigElement x = extract(VolcesConfigElement.CONFIG_ELEMENT_NAME);
        return new VolcesConfigElement(x);
    }
}
