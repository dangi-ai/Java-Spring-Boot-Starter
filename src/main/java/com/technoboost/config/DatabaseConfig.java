package com.technoboost.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("prod")
public class DatabaseConfig {

    private final AwsSecretsManagerService secretsManagerService;
    private final AwsHikariProperties hikariProperties;

    public DatabaseConfig(AwsSecretsManagerService secretsManagerService,
                          AwsHikariProperties hikariProperties) {
        this.secretsManagerService = secretsManagerService;
        this.hikariProperties = hikariProperties;
    }

    @Bean
    public DataSource dataSource() {
        DatabaseSecret secret = secretsManagerService.getDatabaseSecret();

        log.info("Configuring production DataSource for host: {}, db: {}",
                secret.getHost(), secret.getDbName());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(secret.buildJdbcUrl());
        config.setUsername(secret.getUsername());
        config.setPassword(secret.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(hikariProperties.getMaximumPoolSize());
        config.setMinimumIdle(hikariProperties.getMinimumIdle());
        config.setIdleTimeout(hikariProperties.getIdleTimeout());
        config.setConnectionTimeout(hikariProperties.getConnectionTimeout());
        config.setMaxLifetime(hikariProperties.getMaxLifetime());

        config.setPoolName("TechnoBoost-HikariPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }
}
