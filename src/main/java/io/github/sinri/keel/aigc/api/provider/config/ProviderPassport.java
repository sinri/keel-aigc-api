package io.github.sinri.keel.aigc.api.provider.config;

import io.github.sinri.keel.base.configuration.NotConfiguredException;

public interface ProviderPassport {
    String getProviderIdentity();

    String getProviderApiKey() throws NotConfiguredException;

    String getProviderApiSchema() throws NotConfiguredException;

    Integer getProviderApiPort() throws NotConfiguredException;

    String getProviderApiHost() throws NotConfiguredException;

    String getProviderApiPath() throws NotConfiguredException;
}
