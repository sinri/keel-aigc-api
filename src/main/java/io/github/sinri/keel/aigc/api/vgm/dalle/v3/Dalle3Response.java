package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.internal.vgm.dalle.Dalle3ResponseImpl;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface Dalle3Response extends UnmodifiableJsonifiableEntity {
    static Dalle3Response wrap(JsonObject jsonObject) {
        return new Dalle3ResponseImpl(jsonObject);
    }

    default Long created() {
        return Objects.requireNonNull(readLong("created"));
    }

    default @Nullable List<Datum> data() {
        List<JsonObject> array = readJsonObjectArray("data");
        if (array != null) {
            return array.stream().map(Datum::new).toList();
        } else {
            return null;
        }
    }

    default @Nullable Error error() {
        var e = readJsonObject("error");
        if (e == null) return null;
        return new Error(e);
    }

    class Datum extends UnmodifiableJsonifiableEntityImpl {
        public Datum(JsonObject j) {
            super(j);
        }

        public @Nullable String url() {
            return readString("url");
        }

        public @Nullable String revisedPrompt() {
            return readString("revised_prompt");
        }

        public @Nullable DalleContentFilterResults getContentFilterResults() {
            JsonObject jsonObject = readJsonObject("content_filter_results");
            if (jsonObject == null)
                return null;
            return DalleContentFilterResults.wrap(jsonObject);
        }
    }

    class Error extends UnmodifiableJsonifiableEntityImpl {
        public Error(JsonObject j) {
            super(j);
        }

        public @Nullable String code() {
            return readString("code");
        }

        public @Nullable String message() {
            return readString("message");
        }
    }
}
