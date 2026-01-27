package io.github.sinri.keel.aigc.api.llm.sect;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.provider.azure.AzureConfigElement;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeConfigElement;
import io.github.sinri.keel.aigc.api.provider.volces.VolcesConfigElement;


public class ProviderConfigElement extends ConfigElement {

    public ProviderConfigElement(ConfigElement another) {
        super(another);
    }

    public static ProviderConfigElement load() throws NotConfiguredException {
        ConfigElement x = ConfigElement.root().extract("provider");
        return new ProviderConfigElement(x);
    }

    public DashscopeConfigElement dashscope() throws NotConfiguredException {
        ConfigElement x = extract("dashscope");
        return new DashscopeConfigElement(x);
    }

    public AzureConfigElement azure() throws NotConfiguredException {
        ConfigElement x = extract("azure");
        return new AzureConfigElement(x);
    }

    public VolcesConfigElement volces() throws NotConfiguredException {
        ConfigElement x = extract("volces");
        return new VolcesConfigElement(x);
    }
}
