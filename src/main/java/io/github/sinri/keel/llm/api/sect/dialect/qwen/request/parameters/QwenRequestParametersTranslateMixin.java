package io.github.sinri.keel.llm.api.sect.dialect.qwen.request.parameters;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 当您使用翻译模型时需要配置的翻译参数。
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/machine-translation">翻译模型</a>
 */
interface QwenRequestParametersTranslateMixin<E> extends QwenRequestParametersCore<E> {

    /**
     * 源语言的英文全称，详情请参见支持的语言。
     * 您可以将source_lang设置为"auto"，模型会自动判断输入文本属于哪种语言。
     *
     * @param sourceLang 源语言的英文全称，如 "auto" 让模型自动判断。
     */
    default E sourceLang(String sourceLang) {
        ensureEntry("source_lang", sourceLang);
        return this.getImplementation();
    }

    default @Nullable String sourceLang() {
        return readString("source_lang");
    }

    /**
     * 设置目标语言。
     *
     * @param targetLang 目标语言的英文全称。
     */
    default E targetLang(String targetLang) {
        ensureEntry("target_lang", targetLang);
        return this.getImplementation();
    }

    default @Nullable String targetLang() {
        return readString("target_lang");
    }

    /**
     * 在使用术语干预翻译功能时需要设置的术语数组。
     *
     * @param terms 术语对译映射
     */
    default E terms(Map<String, String> terms) {
        JsonArray array = new JsonArray();
        terms.forEach((source, target) -> array.add(new JsonObject().put("source", source).put("target", target)));
        ensureEntry("terms", array);
        return this.getImplementation();
    }

    default Map<String, String> terms() {
        var a = readJsonObjectArray("terms");
        if (a == null) return Map.of();
        Map<String, String> m = new HashMap<>();
        a.forEach(x -> m.put(x.getString("source"), x.getString("target")));
        return m;
    }

    /**
     * 设置翻译记忆数组（tm_list）。
     *
     * @param terms 翻译记忆数组，每项为JsonObject，需包含 source 和 target 字段。
     */
    default E templates(Map<String, String> terms) {
        JsonArray array = new JsonArray();
        terms.forEach((source, target) -> array.add(new JsonObject().put("source", source).put("target", target)));
        ensureEntry("tm_list", array);
        return this.getImplementation();
    }

    default Map<String, String> templates() {
        var a = readJsonObjectArray("tm_list");
        if (a == null) return Map.of();
        Map<String, String> m = new HashMap<>();
        a.forEach(x -> m.put(x.getString("source"), x.getString("target")));
        return m;
    }

    /**
     * 设置领域提示语句（domains）。
     *
     * @param domains 领域提示语句，仅支持英文。
     */
    default E domains(String domains) {
        ensureEntry("domains", domains);
        return this.getImplementation();
    }

    default @Nullable String domains() {
        return readString("domains");
    }
}
