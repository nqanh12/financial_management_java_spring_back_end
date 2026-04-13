package com.financialmanagement.expense.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialmanagement.expense.application.dto.report.DashboardSummaryResponse;
import com.financialmanagement.expense.application.dto.report.MonthlyReportResponse;
import com.financialmanagement.expense.application.port.out.ReportingCachePort;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisReportingCacheAdapter implements ReportingCachePort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.reporting-ttl:PT10M}")
    private Duration ttl;

    private static String dashKey(UUID userId, String yearMonth) {
        return "emo:dashboard:" + userId + ":" + yearMonth;
    }

    private static String reportKey(UUID userId, String yearMonth) {
        return "emo:report:" + userId + ":" + yearMonth;
    }

    @Override
    public Optional<DashboardSummaryResponse> getDashboard(UUID userId, String yearMonth) {
        String json = stringRedisTemplate.opsForValue().get(dashKey(userId, yearMonth));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, DashboardSummaryResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize dashboard cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putDashboard(UUID userId, String yearMonth, DashboardSummaryResponse value) {
        try {
            stringRedisTemplate
                    .opsForValue()
                    .set(dashKey(userId, yearMonth), objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize dashboard cache", e);
        }
    }

    @Override
    public Optional<MonthlyReportResponse> getMonthlyReport(UUID userId, String yearMonth) {
        String json = stringRedisTemplate.opsForValue().get(reportKey(userId, yearMonth));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, MonthlyReportResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize report cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putMonthlyReport(UUID userId, String yearMonth, MonthlyReportResponse value) {
        try {
            stringRedisTemplate
                    .opsForValue()
                    .set(reportKey(userId, yearMonth), objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize report cache", e);
        }
    }

    @Override
    public void evictUserReports(UUID userId) {
        var dash = stringRedisTemplate.keys("emo:dashboard:" + userId + ":*");
        if (dash != null && !dash.isEmpty()) {
            stringRedisTemplate.delete(dash);
        }
        var rep = stringRedisTemplate.keys("emo:report:" + userId + ":*");
        if (rep != null && !rep.isEmpty()) {
            stringRedisTemplate.delete(rep);
        }
    }
}
