package io.github.sinri.keel.aigc.api.provider.config;

import io.github.sinri.keel.base.configuration.NotConfiguredException;

public interface ModelPassport {
    String getModelIdentity();

    String getModelCode() throws NotConfiguredException;

    String getModelApiKey() throws NotConfiguredException;

    String getModelApiSchema() throws NotConfiguredException;

    Integer getModelApiPort() throws NotConfiguredException;

    String getModelApiHost() throws NotConfiguredException;

    String getModelApiPath() throws NotConfiguredException;
}
