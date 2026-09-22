package io.github.sinri.keel.aigc.api.trace;

import io.vertx.core.json.JsonObject;

/** Blocking persistence, invoked only on the recorder's worker. Implementations must finish in bounded time. */
@FunctionalInterface
public interface CatholicTraceSink {
    String save(JsonObject trace) throws Exception;

    /** Optional retention work, serialized with saves on the recorder worker. */
    default void maintenance() throws Exception {}

}
