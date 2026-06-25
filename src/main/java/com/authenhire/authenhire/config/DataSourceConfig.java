package com.authenhire.authenhire.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @Qualifier("dataSource")
    public DataSource dataSource(Environment environment) {
        HikariDataSource dataSource = new HikariDataSource();

        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        if (url != null && (url.startsWith("postgresql://") || url.startsWith("postgres://"))) {
            try {
                URI uri = URI.create(url);
                String userInfo = uri.getUserInfo();
                if ((username == null || username.isBlank() || "sa".equals(username)) && userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    password = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                if (host != null && !host.isBlank()) {
                    jdbcUrl.append(host);
                }
                if (port > 0) {
                    jdbcUrl.append(':').append(port);
                }
                if (path != null && !path.isBlank()) {
                    jdbcUrl.append(path);
                }
                if (query != null && !query.isBlank()) {
                    jdbcUrl.append('?').append(query);
                }
                url = jdbcUrl.toString();
            } catch (Exception ignored) {
                if (url != null && url.startsWith("postgresql://")) {
                    url = "jdbc:" + url;
                }
            }
        } else if (url != null && url.startsWith("jdbc:postgresql://")) {
            try {
                URI uri = URI.create(url.replaceFirst("^jdbc:", ""));
                String userInfo = uri.getUserInfo();
                if ((username == null || username.isBlank() || "sa".equals(username)) && userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    password = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                if (host != null && !host.isBlank()) {
                    jdbcUrl.append(host);
                }
                if (port > 0) {
                    jdbcUrl.append(':').append(port);
                }
                if (path != null && !path.isBlank()) {
                    jdbcUrl.append(path);
                }
                if (query != null && !query.isBlank()) {
                    jdbcUrl.append('?').append(query);
                }
                url = jdbcUrl.toString();
            } catch (Exception ignored) {
                // Fall back to the original value.
            }
        }

        dataSource.setJdbcUrl(url);
        if (username != null && !username.isBlank()) {
            dataSource.setUsername(username);
        }
        if (password != null) {
            dataSource.setPassword(password);
        }

        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
        if (driverClassName != null && !driverClassName.isBlank()) {
            dataSource.setDriverClassName(driverClassName);
        }

        return dataSource;
    }
}
