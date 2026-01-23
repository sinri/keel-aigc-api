package io.github.sinri.keel.llm.api.catholic.tool.definition;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonFunctionToolDefinition;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.json.schema.common.dsl.Schemas;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FunctionToolDefinition extends JsonifiableDataUnit {
    static FunctionToolDefinition create() {
        return new CommonFunctionToolDefinition();
    }

    /**
     * @return 工具函数的名称
     */
    default String name() {
        return readStringRequired("name");
    }

    /**
     * @param name 工具函数的名称
     */
    default FunctionToolDefinition name(String name) {
        ensureEntry("name", name);
        return this;
    }

    /**
     * @return 工具函数的描述，供模型选择何时以及如何调用工具函数。
     */
    default String description() {
        return readStringRequired("description");
    }

    /**
     * @param description 工具函数的描述，供模型选择何时以及如何调用工具函数。
     */
    default FunctionToolDefinition description(String description) {
        ensureEntry("description", description);
        return this;
    }

    /**
     * @return 工具的参数描述，需要是一个合法的JSON Schema。
     *         如果parameters参数为空，表示function没有入参。
     * @see <a href="https://json-schema.org/understanding-json-schema">JSON
     *         Schema</a>
     */
    default JsonObject parameters() {
        return readJsonObjectRequired("parameters");
    }

    /**
     * @param parameters 工具的参数描述，需要是一个合法的JSON Schema。
     * @see <a href="https://json-schema.org/understanding-json-schema">JSON
     *         Schema</a>
     */
    default FunctionToolDefinition parameters(@Nullable JsonObject parameters) {
        ensureEntry("parameters", parameters);
        return this;
    }

    /**
     * @param handler 给定一个JSON Schema的Builder，在此处理器里将其完善为工具的参数描述。
     */
    default FunctionToolDefinition parameters(Handler<ObjectSchemaBuilder> handler) {
        ObjectSchemaBuilder objectSchemaBuilder = Schemas.objectSchema();
        handler.handle(objectSchemaBuilder);
        return parameters(objectSchemaBuilder.toJson());
    }

    /**
     * 通过参数定义列表设置工具的参数描述。
     * 如果参数定义列表为空，则设置parameters为null，表示函数没有入参。
     *
     * @param parameterDefinitions 参数定义列表，每个元素包含参数的类型、名称、描述和是否必需等信息
     * @return 当前FunctionToolDefinition实例，支持链式调用
     */
    default FunctionToolDefinition parameters(List<FunctionParameterDefinition> parameterDefinitions) {
        if (parameterDefinitions.isEmpty()) {
            return parameters((JsonObject) null);
        }
        return parameters(builder -> {
            parameterDefinitions.forEach(parameterDefinition -> {
                var x = Schemas.schema()
                               .type(parameterDefinition.getType())
                               .withKeyword("description", parameterDefinition.getDescription());
                if (parameterDefinition.getRequired() != null) {
                    builder.requiredProperty(parameterDefinition.getName(), x);
                } else {
                    builder.property(parameterDefinition.getName(), x);
                }
            });

        });
    }
}
