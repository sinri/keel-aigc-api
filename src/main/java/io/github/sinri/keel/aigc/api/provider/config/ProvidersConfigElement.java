package io.github.sinri.keel.aigc.api.provider.config;

import io.github.sinri.keel.aigc.api.provider.azure.AzureProviderConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;

/**
 * Configuration entrance, i.e. the config root endpoint of all providers.
 */
public class ProvidersConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "providers";

    ProvidersConfigElement(ConfigElement another) {
        super(another);
    }

    public static ProvidersConfigElement load(List<String> providersConfigKeychain) throws NotConfiguredException {
        return new ProvidersConfigElement(ConfigElement.root().extract(providersConfigKeychain));
    }

    public static ProvidersConfigElement load() throws NotConfiguredException {
        return load(List.of(CONFIG_ELEMENT_NAME));
    }

    public AzureProviderConfigElement azure() throws NotConfiguredException {
        return new AzureProviderConfigElement(getChild("azure"));
    }
}
