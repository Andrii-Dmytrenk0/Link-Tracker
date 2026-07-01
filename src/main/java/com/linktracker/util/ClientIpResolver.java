package com.linktracker.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP address, taking common reverse-proxy headers
 * (set by Nginx/load balancers) into account.
 */
public final class ClientIpResolver {

    private static final String[] HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "CF-Connecting-IP"
    };

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        for (String header : HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For may contain a comma-separated chain; the
                // first entry is the original client.
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
