package com.skillbarter.common.security;

import com.skillbarter.common.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Map<String, Queue<Long>> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Queue<Long>> registerAttempts = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.login-max-requests:10}")
    private int loginMaxRequests;

    @Value("${app.rate-limit.login-window-minutes:15}")
    private int loginWindowMinutes;

    @Value("${app.rate-limit.register-max-requests:5}")
    private int registerMaxRequests;

    @Value("${app.rate-limit.register-window-minutes:60}")
    private int registerWindowMinutes;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (path.endsWith("/api/v1/auth/login")) {
                checkRateLimit(clientIp, loginAttempts, loginMaxRequests, loginWindowMinutes * 60 * 1000L, "login");
            } else if (path.endsWith("/api/v1/auth/register")) {
                checkRateLimit(clientIp, registerAttempts, registerMaxRequests, registerWindowMinutes * 60 * 1000L, "registration");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void checkRateLimit(
            String clientIp,
            Map<String, Queue<Long>> attemptsMap,
            int maxRequests,
            long windowMs,
            String actionName) {

        long now = System.currentTimeMillis();
        Queue<Long> timestamps = attemptsMap.computeIfAbsent(clientIp, k -> new ConcurrentLinkedQueue<>());

        while (!timestamps.isEmpty() && (now - timestamps.peek() > windowMs)) {
            timestamps.poll();
        }

        if (timestamps.size() >= maxRequests) {
            log.warn("Rate limit exceeded for IP {} on {} endpoint", clientIp, actionName);
            throw new RateLimitExceededException("Too many " + actionName + " attempts from this IP. Please try again later.");
        }

        timestamps.add(now);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
