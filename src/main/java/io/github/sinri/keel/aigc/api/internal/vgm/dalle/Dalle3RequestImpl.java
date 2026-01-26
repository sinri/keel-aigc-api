package io.github.sinri.keel.aigc.api.internal.vgm.dalle;

import io.github.sinri.keel.aigc.api.vgm.dalle.v3.Dalle3Kit;
import io.github.sinri.keel.aigc.api.vgm.dalle.v3.Dalle3Request;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;

import java.util.UUID;


public class Dalle3RequestImpl extends JsonifiableDataUnitImpl implements Dalle3Request {

    public Dalle3RequestImpl(JsonObject jsonObject) {
        super(jsonObject);
        try {
            getRequestId();
        } catch (NullPointerException nullPointerException) {
            setRequestId(UUID.randomUUID().toString());
        }
    }

    public Dalle3RequestImpl() {
        super();
        setN(1);
        setQuality(Dalle3Kit.Dalle3Quality.standard);
        setStyle(Dalle3Kit.Dalle3Style.natural);
        setSize(Dalle3Kit.Dalle3Size.SQUARE);
        setRequestId(UUID.randomUUID().toString());
    }
}
