package com.financialmanagement.expense.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Maps Render/Heroku-style {@code DATABASE_URL} ({@code postgresql://user:pass@host:port/db}) to
 * {@code spring.datasource.*}. Also accepts a full {@code jdbc:postgresql:...} URL in {@code DATABASE_URL}.
 *
 * <p>Explicit {@code JDBC_DATABASE_URL} (full JDBC) is left to {@code application.yaml} and is not
 * overwritten here.
 */
public class PostgresDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SOURCE_NAME = "postgresDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (StringUtils.hasText(environment.getProperty("JDBC_DATABASE_URL"))) {
            return;
        }
        String raw = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(raw)) {
            return;
        }
        raw = raw.trim();

        Map<String, Object> map = new LinkedHashMap<>();
        if (raw.startsWith("jdbc:")) {
            map.put("spring.datasource.url", raw);
        } else if (raw.startsWith("postgres://") || raw.startsWith("postgresql://")) {
            Parsed p = parsePostgresUri(raw);
            String jdbcUrl = buildJdbcUrl(p, environment);
            map.put("spring.datasource.url", jdbcUrl);
            map.put("spring.datasource.username", p.username());
            map.put("spring.datasource.password", p.password());
        } else {
            return;
        }

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, map));
    }

    private static String buildJdbcUrl(Parsed p, ConfigurableEnvironment environment) {
        String sslMode = environment.getProperty("DATABASE_SSL_MODE");
        if (!StringUtils.hasText(sslMode)) {
            sslMode = "require";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:postgresql://").append(p.host()).append(":").append(p.port()).append("/").append(p.database());
        String q = p.query();
        if (StringUtils.hasText(q)) {
            sb.append("?").append(q);
            if (!containsParam(q, "sslmode")) {
                sb.append("&sslmode=").append(sslMode);
            }
        } else {
            sb.append("?sslmode=").append(sslMode);
        }
        return sb.toString();
    }

    private static boolean containsParam(String query, String name) {
        return query.contains(name + "=") || query.contains("&" + name + "=");
    }

    private static Parsed parsePostgresUri(String raw) {
        String normalized = raw.replaceFirst("^postgres://", "postgresql://");
        URI uri = URI.create(normalized);
        String userInfo = uri.getRawUserInfo();
        String username = "";
        String password = "";
        if (StringUtils.hasText(userInfo)) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = urlDecode(userInfo.substring(0, colon));
                password = urlDecode(userInfo.substring(colon + 1));
            } else {
                username = urlDecode(userInfo);
            }
        }
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw new IllegalStateException("DATABASE_URL must include a host");
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getRawPath();
        if (!StringUtils.hasText(path) || "/".equals(path)) {
            throw new IllegalStateException("DATABASE_URL must include a database name in the path");
        }
        String database = path.startsWith("/") ? path.substring(1) : path;
        String query = uri.getRawQuery();
        return new Parsed(host, port, database, query, username, password);
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private record Parsed(String host, int port, String database, String query, String username, String password) {}

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
