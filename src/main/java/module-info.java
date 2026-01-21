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
}