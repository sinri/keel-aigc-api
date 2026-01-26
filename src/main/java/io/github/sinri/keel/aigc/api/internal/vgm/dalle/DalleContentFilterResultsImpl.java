package io.github.sinri.keel.aigc.api.internal.vgm.dalle;

import io.github.sinri.keel.aigc.api.vgm.dalle.v3.DalleContentFilterResults;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


public class DalleContentFilterResultsImpl extends UnmodifiableJsonifiableEntityImpl implements DalleContentFilterResults {
    public DalleContentFilterResultsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
