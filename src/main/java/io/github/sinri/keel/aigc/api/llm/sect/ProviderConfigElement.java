package io.github.sinri.keel.aigc.api.llm.sect;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.provider.azure.AzureConfigElement;
import io.github.sinri.keel.aigc.api.provider.dashscope.DashscopeConfigElement;
import io.github.sinri.keel.aigc.api.provider.volces.VolcesConfigElement;

import java.util.List;

public class ProviderConfigElement extends ConfigElement {

    public ProviderConfigElement(ConfigElement another) {
        super(another);
    }

    public static ProviderConfigElement load() throws NotConfiguredException {
        ConfigElement x = ConfigElement.root().extract("provider");
        if (x == null) {
            throw new NotConfiguredException(List.of(), "provider");
        }
        return new ProviderConfigElement(x);
    }

    public DashscopeConfigElement dashscope() throws NotConfiguredException {
        ConfigElement x = extract("dashscope");
        if (x == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "dashscope");
        }
        return new DashscopeConfigElement(x);
    }

    public AzureConfigElement azure() throws NotConfiguredException {
        ConfigElement x = extract("azure");
        if (x == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "azure");
        }
        return new AzureConfigElement(x);
    }

    public VolcesConfigElement volces() throws NotConfiguredException {
        ConfigElement x = extract("volces");
        if (x == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "volces");
        }
        return new VolcesConfigElement(x);
    }
}
