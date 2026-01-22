package io.github.sinri.keel.llm.api.sect;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.sect.provider.azure.AzureConfigElement;
import io.github.sinri.keel.llm.api.sect.provider.dashscope.DashscopeConfigElement;

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
}
