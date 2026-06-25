package com.authenhire.authenhire.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Qualifier("dataSource")
    public DataSource dataSource(Environment environment) {
        HikariDataSource dataSource = new HikariDataSource();

        String url = environment.getProperty("spring.datasource.url");
        if (url != null && url.startsWith("postgresql://")) {
            url = "jdbc:" + url;
        }

        dataSource.setJdbcUrl(url);
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));

        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
        if (driverClassName != null && !driverClassName.isBlank()) {
            dataSource.setDriverClassName(driverClassName);
        }

        return dataSource;
    }
}
