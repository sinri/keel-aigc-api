package io.github.sinri.keel.llm.api.catholic.tool.definition;

import io.vertx.json.schema.common.dsl.SchemaType;
import org.jspecify.annotations.Nullable;

/**
 * FunctionParameterDefinition 用于描述函数参数的定义。
 * 包括参数类型、名称和描述信息，常用于函数元数据、自动化文档生成等场景。
 */
public class FunctionParameterDefinition {
    /**
     * 参数的数据类型，对应 JSON Schema 的类型。
     */
    private SchemaType type;

    /**
     * 参数名称。
     */
    private String name;

    /**
     * 参数的详细描述。
     */
    private String description;

    private @Nullable Boolean required;

    /**
     * 构造方法，初始化参数定义。
     * @param type 参数类型（JSON Schema 类型）
     * @param name 参数名称
     * @param description 参数描述
     */
    public FunctionParameterDefinition(SchemaType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    /**
     * 获取参数类型。
     * @return 参数类型
     */
    public SchemaType getType() {
        return type;
    }

    /**
     * 设置参数类型。
     * @param type 参数类型
     */
    public void setType(SchemaType type) {
        this.type = type;
    }

    /**
     * 获取参数名称。
     * @return 参数名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置参数名称。
     * @param name 参数名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取参数描述。
     * @return 参数描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置参数描述。
     * @param description 参数描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public @Nullable Boolean getRequired() {
        return required;
    }
}
