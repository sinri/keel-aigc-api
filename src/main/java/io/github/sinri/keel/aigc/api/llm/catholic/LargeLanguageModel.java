package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.json.JsonObject;


/**
 * 需要针对具体的模型生成具体的实现类。
 */
public abstract class LargeLanguageModel implements JsonObjectConvertible {
    /**
     * 针对一个具体的大语言模型，提供一个代码标识，用于在 {@link LLMServiceFacade} 中进行登记和管理。
     * <p>
     * 注意，与各大语言模型服务所提供的模型代码（如参数中的 {@code model})具有不同的定义。
     *
     * @return 对应大语言模型在 {@link LLMServiceFacade} 登记的代码标识
     */
    public abstract String getRegisterCode();

    /**
     * 构建当前大语言模型类实例所对应的大语言模型服务类实例。
     * @return 对应的大语言模型服务类实例
     * @throws NotConfiguredException 相关配置不全
     */
    public abstract LLMService getService() throws NotConfiguredException;

    @Override
    public final JsonObject toJsonObject() {
        return new JsonObject().put("code", getRegisterCode());
    }

    @Override
    public final String toJsonExpression() {
        return toJsonObject().encode();
    }

    @Override
    public final String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    public final Logger getLogger() {
        return LLMServiceFacade.getLogger();
    }
}
