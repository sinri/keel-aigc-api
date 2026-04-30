package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 工具函数定义
 */
public interface FunctionDefinition {

    String name();

    String description();

    @Nullable JsonObject parameters();

    /**
     * 创建 Builder。
     */
    static Builder builder() {
        return FunctionDefinitionImpl.builder();
    }

    /**
     * 创建函数定义
     */
    static FunctionDefinition of(String name, String description, @Nullable JsonObject parameters) {
        return new FunctionDefinitionImpl(name, description, parameters);
    }

    /**
     * 创建无参数函数定义
     */
    static FunctionDefinition of(String name, String description) {
        return new FunctionDefinitionImpl(name, description, new JsonObject());
    }

    /**
     * 用于链式构造 {@link FunctionDefinition} 的构建器。
     * <p>
     * 未显式调用 {@link #parameters(JsonObject)} 时，可通过 {@link #addParameter} 逐步拼装
     * 符合 JSON Schema 风格的 {@code object} 形参（含 {@code type}、{@code properties}、可选 {@code required}）。
     * <p>
     * 实现类会对外部传入的 {@link JsonObject} 做拷贝，避免调用方后续修改影响已构建的定义。
     */
    interface Builder {

        /**
         * 设置工具函数在协议侧使用的名称（通常与路由/注册名一致）。
         *
         * @param name 函数名，不可为 {@code null}、空串或仅空白；{@link #build()} 时会校验
         * @return 当前构建器，便于链式调用
         */
        Builder name(String name);

        /**
         * 设置对该工具的人类可读说明，供模型决定是否及如何调用。
         *
         * @param description 函数说明，不可为 {@code null}、空串或仅空白；{@link #build()} 时会校验
         * @return 当前构建器，便于链式调用
         */
        Builder description(String description);

        /**
         * {@link #description(String)} 的语义别名，便于按“注释/说明”一词链式书写。
         * <p>
         * 行为与 {@code description(comment)} 相同。
         *
         * @param comment 与 {@code description} 相同语义的说明文案
         * @return 当前构建器，便于链式调用
         */
        Builder comment(String comment);

        /**
         * 整体替换形参 JSON Schema（通常为根类型 {@code object} 的对象）。
         * <p>
         * 传入非 {@code null} 时会拷贝一份再保存；传入 {@code null} 表示尚未指定 schema，
         * 后续若调用 {@link #addParameter}，构建器会自动补全为 {@code object} 及 {@code properties} 结构。
         *
         * @param parameters 完整的 parameters 对象，或 {@code null} 留待由 {@link #addParameter} 推导
         * @return 当前构建器，便于链式调用
         */
        Builder parameters(@Nullable JsonObject parameters);

        /**
         * 追加一个简单类型的形参：内部会展开为带 {@code type} 与 {@code description} 的属性 schema，
         * 默认不作为 {@code required} 成员。
         *
         * @param name        参数名，对应 {@code properties} 下的键
         * @param type        JSON Schema 类型字符串，例如 {@code "string"}、{@code "number"}
         * @param description 该参数的人类可读说明
         * @return 当前构建器，便于链式调用
         * @throws IllegalArgumentException 当 {@code name} 为 {@code null}、空串或仅空白时
         */
        Builder addParameter(String name, String type, String description);

        /**
         * 追加一个简单类型的形参，并可指定是否加入根对象的 {@code required} 数组。
         *
         * @param name        参数名，对应 {@code properties} 下的键
         * @param type        JSON Schema 类型字符串，例如 {@code "string"}、{@code "integer"}
         * @param description 该参数的人类可读说明
         * @param required    为 {@code true} 时将该 {@code name} 记入 {@code required}（去重追加）
         * @return 当前构建器，便于链式调用
         * @throws IllegalArgumentException 当 {@code name} 为 {@code null}、空串或仅空白时
         */
        Builder addParameter(String name, String type, String description, boolean required);

        /**
         * 追加一个任意 JSON Schema 片段作为某属性的定义，默认不作为 {@code required} 成员。
         * <p>
         * {@code schema} 会被拷贝后再写入，可包含 {@code enum}、{@code oneOf} 等任意合法字段。
         *
         * @param name   参数名，对应 {@code properties} 下的键
         * @param schema 该属性的完整 schema 对象，不可为 {@code null}
         * @return 当前构建器，便于链式调用
         * @throws IllegalArgumentException 当 {@code name} 非法或 {@code schema} 为 {@code null} 时
         */
        Builder addParameter(String name, JsonObject schema);

        /**
         * 追加一个任意 JSON Schema 片段作为某属性的定义，并可指定是否加入根对象的 {@code required} 数组。
         * <p>
         * {@code schema} 会被拷贝后再写入。
         *
         * @param name     参数名，对应 {@code properties} 下的键
         * @param schema   该属性的完整 schema 对象，不可为 {@code null}
         * @param required 为 {@code true} 时将该 {@code name} 记入 {@code required}（去重追加）
         * @return 当前构建器，便于链式调用
         * @throws IllegalArgumentException 当 {@code name} 非法或 {@code schema} 为 {@code null} 时
         */
        Builder addParameter(String name, JsonObject schema, boolean required);

        /**
         * 根据当前设置生成不可变语义的 {@link FunctionDefinition} 实例。
         * <p>
         * 若从未调用 {@link #parameters(JsonObject)} 且也未通过 {@link #addParameter} 写入任何属性，
         * 则 {@link FunctionDefinition#parameters()} 为空的 JSON 对象 {@code {}}，表示无额外形参。
         *
         * @return 构建完成的函数定义
         * @throws IllegalArgumentException 当函数 {@code name} 或 {@code description}（含经 {@link #comment(String)} 设置者）
         *                                  未设置、为空串或仅空白时
         */
        FunctionDefinition build();
    }
}
