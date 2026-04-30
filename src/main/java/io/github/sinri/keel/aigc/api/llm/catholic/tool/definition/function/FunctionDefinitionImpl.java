package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import io.vertx.core.json.JsonArray;

/**
 * 工具函数定义的实现。
 */
public record FunctionDefinitionImpl(
    String name,
    String description,
    @Nullable JsonObject parameters
) implements FunctionDefinition {

    /**
     * 创建函数定义
     */
    public static FunctionDefinition of(String name, String description, @Nullable JsonObject parameters) {
        return new FunctionDefinitionImpl(name, description, parameters);
    }

    /**
     * 创建无参数函数定义
     */
    public static FunctionDefinition of(String name, String description) {
        return new FunctionDefinitionImpl(name, description, new JsonObject());
    }

    /**
     * 创建 Builder。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 函数定义 Builder。
     */
    public static class Builder implements FunctionDefinition.Builder {
        private String name;
        private String description;
        private JsonObject parameters;

        @Override
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder comment(String comment) {
            return description(comment);
        }

        @Override
        public Builder parameters(@Nullable JsonObject parameters) {
            this.parameters = parameters == null ? null : parameters.copy();
            return this;
        }

        @Override
        public Builder addParameter(String name, String type, String description) {
            return addParameter(name, type, description, false);
        }

        @Override
        public Builder addParameter(String name, String type, String description, boolean required) {
            return addParameter(name, new JsonObject()
                .put("type", type)
                .put("description", description), required);
        }

        @Override
        public Builder addParameter(String name, JsonObject schema) {
            return addParameter(name, schema, false);
        }

        @Override
        public Builder addParameter(String name, JsonObject schema, boolean required) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("parameter name is required");
            }
            if (schema == null) {
                throw new IllegalArgumentException("parameter schema is required");
            }

            JsonObject objectSchema = ensureObjectParameters();
            objectSchema.getJsonObject("properties").put(name, schema.copy());
            if (required) {
                addRequiredParameter(objectSchema, name);
            }
            return this;
        }

        @Override
        public FunctionDefinitionImpl build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description is required");
            }
            return new FunctionDefinitionImpl(name, description, parameters == null ? new JsonObject() : parameters.copy());
        }

        private JsonObject ensureObjectParameters() {
            if (parameters == null) {
                parameters = new JsonObject();
            }
            if (parameters.getString("type") == null) {
                parameters.put("type", "object");
            }
            if (parameters.getJsonObject("properties") == null) {
                parameters.put("properties", new JsonObject());
            }
            return parameters;
        }

        private void addRequiredParameter(JsonObject objectSchema, String name) {
            JsonArray required = objectSchema.getJsonArray("required");
            if (required == null) {
                required = new JsonArray();
                objectSchema.put("required", required);
            }
            if (!required.contains(name)) {
                required.add(name);
            }
        }
    }
}
