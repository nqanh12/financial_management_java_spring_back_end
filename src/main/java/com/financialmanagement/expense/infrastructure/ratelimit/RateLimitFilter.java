package com.financialmanagement.expense.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialmanagement.expense.infrastructure.security.JwtUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final DefaultRedisScript<Long> RATE_SCRIPT = new DefaultRedisScript<>();

    static {
        RATE_SCRIPT.setScriptText(
                """
                        local c = redis.call('INCR', KEYS[1])
                        if c == 1 then
                          redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
                        end
                        return c
                        """);
        RATE_SCRIPT.setResultType(Long.class);
    }

    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ObjectMapper objectMapper;
    private final int maxRequestsPerMinute;
    private final int windowSeconds;

    public RateLimitFilter(
            ObjectProvider<StringRedisTemplate> redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.requests-per-minute:120}") int maxRequestsPerMinute,
            @Value("${app.rate-limit.window-seconds:60}") int windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        StringRedisTemplate redis = redisTemplate.getIfAvailable();
        if (redis == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            long window = Instant.now().getEpochSecond() / windowSeconds;
            String key = "emo:rl:v1:" + window + ":" + ratePartitionKey(request);
            Long count = redis.execute(RATE_SCRIPT, List.of(key), String.valueOf(windowSeconds + 5));
            if (count != null && count > maxRequestsPerMinute) {
                int retryAfter = windowSeconds - (int) (Instant.now().getEpochSecond() % windowSeconds);
                if (retryAfter <= 0) {
                    retryAfter = windowSeconds;
                }
                writeTooManyRequests(response, retryAfter);
                return;
            }
        } catch (Exception ex) {
            log.warn("Rate limit check skipped (Redis error): {}", ex.toString());
        }
        filterChain.doFilter(request, response);
    }

    private String ratePartitionKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof JwtUserPrincipal p) {
            return "u:" + p.userId();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, int retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "message", "Rate limit exceeded");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
