package com.assessment.card_product_management.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiRequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestResponseLoggingFilter.class);
    private static final int MAX_LOGGED_BODY_LENGTH = 4_096;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, MAX_LOGGED_BODY_LENGTH);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000;
            LOGGER.info(
                    "api_request method={} path={} query={} body={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    queryForLog(request.getQueryString()),
                    bodyForLog(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding())
            );
            LOGGER.info(
                    "api_response method={} path={} status={} body={} duration_ms={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    responseWrapper.getStatus(),
                    bodyForLog(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding()),
                    durationMillis
            );
            responseWrapper.copyBodyToResponse();
        }
    }

    private String bodyForLog(byte[] body, String characterEncoding) {
        if (body.length == 0) {
            return "<empty>";
        }
        String payload = new String(
                body,
                StringUtils.hasText(characterEncoding)
                        ? java.nio.charset.Charset.forName(characterEncoding)
                        : java.nio.charset.StandardCharsets.UTF_8
        );
        String boundedPayload = payload.length() > MAX_LOGGED_BODY_LENGTH
                ? payload.substring(0, MAX_LOGGED_BODY_LENGTH) + "…<truncated>"
                : payload;
        return boundedPayload.replaceAll(
                "(?i)(\\\"(?:password|token|authorization|cookie|api[_-]?key)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
                "$1[REDACTED]$2"
        ).replace("\r", "\\r").replace("\n", "\\n");
    }

    private String queryForLog(String query) {
        if (!StringUtils.hasText(query)) {
            return "<empty>";
        }
        return query.replaceAll(
                "(?i)((?:^|&)(?:password|token|authorization|cookie|api[_-]?key)=)[^&]*",
                "$1[REDACTED]"
        );
    }
}
