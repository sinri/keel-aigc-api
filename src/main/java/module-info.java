module io.github.sinri.keel.integration.llm.api {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;

    requires transitive io.vertx.core;
    requires transitive io.vertx.web.client;
    requires transitive io.vertx.config;
    requires transitive org.commonmark;
    requires transitive org.commonmark.ext.gfm.tables;

    requires static org.jspecify;
    requires io.vertx.jsonschema;
    requires java.naming;
    requires java.logging;

    // io.github.sinri.keel.llm.api.internal 及其下子包均不对外透出
    exports io.github.sinri.keel.llm.api.catholic;
    exports io.github.sinri.keel.llm.api.catholic.message;
    exports io.github.sinri.keel.llm.api.catholic.request;
    exports io.github.sinri.keel.llm.api.catholic.response;
    exports io.github.sinri.keel.llm.api.catholic.response.stream;
    exports io.github.sinri.keel.llm.api.catholic.tool;
    exports io.github.sinri.keel.llm.api.catholic.tool.call;
    exports io.github.sinri.keel.llm.api.catholic.tool.definition;
    exports io.github.sinri.keel.llm.api.sect;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao.message;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao.request;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream;
    exports io.github.sinri.keel.llm.api.sect.dialect.doubao.response.sync;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.core;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.core.error;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.message;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.message.vision;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.request;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.response.stream;
    exports io.github.sinri.keel.llm.api.sect.dialect.openai.response.sync;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.message;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.message.vision;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.request;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.request.input;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.request.parameters;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.response.stream;
    exports io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync;
    exports io.github.sinri.keel.llm.api.sect.provider;
    exports io.github.sinri.keel.llm.api.sect.provider.azure;
    exports io.github.sinri.keel.llm.api.sect.provider.dashscope;
    exports io.github.sinri.keel.llm.api.sect.provider.volces;


}