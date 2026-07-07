package com.example.Multi_reward.util;

import org.slf4j.MDC;

public final class LogContext {

    public static final String TRACE_ID = "traceId";
    public static final String USER_ID  = "userId";

    private LogContext() {}

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void setUserId(int userId) {
        MDC.put(USER_ID, String.valueOf(userId));
    }

    public static void clear() {
        MDC.clear();
    }
}