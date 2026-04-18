package com.offerdungeon.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestTraceContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_ATTRIBUTE =
            RequestTraceContext.class.getName() + ".requestId";
    private static final String UNKNOWN_REQUEST_ID = "N/A";

    private RequestTraceContext() {}

    public static String getRequestId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object requestId = request.getAttribute(REQUEST_ID_ATTRIBUTE);
            if (requestId instanceof String value && !value.isBlank()) {
                return value;
            }
        }
        return UNKNOWN_REQUEST_ID;
    }
}
