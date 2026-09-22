package io.github.sinri.keel.aigc.api.trace;

import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable, explicit correlation; never stored in thread-local state or sent to a provider. */
public final class CatholicTraceContext {
    private static final CatholicTraceContext NONE = new CatholicTraceContext(null, Map.of());
    private final CatholicTraceSession session;
    private final Map<String, Object> fields;

    CatholicTraceContext(CatholicTraceSession session, Map<String, Object> fields) {
        this.session = session;
        this.fields = Map.copyOf(fields);
    }

    public static CatholicTraceContext none() { return NONE; }
    public boolean enabled() { return session != null; }
    public String traceId() { return session == null ? "" : session.traceId(); }

    public CatholicTraceContext child(String key, Object value) {
        if (session == null) return this;
        var copy = new LinkedHashMap<>(fields);
        copy.put(key, value);
        return new CatholicTraceContext(session, copy);
    }

    public void event(String kind, Map<String, ?> metadata) { payload(kind, metadata, null, false); }

    /** Arguments are distinguished from other payloads so structure-only capture can preserve their layout. */
    public void payload(String kind, Map<String, ?> metadata, String text, boolean arguments) {
        if (session != null) try { session.record(kind, fields, metadata, text, arguments); }
        catch (Throwable failure) { session.captureFailed(); }
    }

    /** Record the earliest failing stage; lifecycle completion is owned by the Agent or explicit session. */
    public void failure(String stage, Throwable cause) {
        if (session != null) try { session.failure(this, stage, cause); }
        catch (Throwable failure) { session.captureFailed(); }
    }
}
