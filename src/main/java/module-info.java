module io.github.sinri.keel.integration.llm.api {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;

    requires transitive io.vertx.core;
    requires transitive io.vertx.config;
    requires transitive org.commonmark;
    requires transitive org.commonmark.ext.gfm.tables;

    requires static org.jspecify;
    requires io.vertx.jsonschema;
    requires org.yaml.snakeyaml;

    // io.github.sinri.keel.aigc.api.internal 及其下子包均不对外透出

    // Catholic LLM 通用格式
    exports io.github.sinri.keel.aigc.api.llm.catholic;
    exports io.github.sinri.keel.aigc.api.llm.catholic.message;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool;
    exports io.github.sinri.keel.aigc.api.llm.catholic.request;
    exports io.github.sinri.keel.aigc.api.llm.catholic.response;

    // OpenAI Chat Completions API
    exports io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

    // DashScope API
    exports io.github.sinri.keel.aigc.api.llm.dashscope;
}
