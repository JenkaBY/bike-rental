package com.github.jenkaby.bikerental.shared.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class WebUtils {

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private WebUtils() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(forwardedFor)) {
            for (String token : forwardedFor.split(",")) {
                var trimmed = token.trim();
                if (StringUtils.hasText(trimmed)) {
                    return trimmed;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
