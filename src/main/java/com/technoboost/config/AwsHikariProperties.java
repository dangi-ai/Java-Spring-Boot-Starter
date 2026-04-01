package com.technoboost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@Profile("prod")
@ConfigurationProperties(prefix = "aws.datasource.hikari")
public class AwsHikariProperties {

    private int maximumPoolSize = 20;
    private int minimumIdle = 5;
    private long idleTimeout = 300000;
    private long connectionTimeout = 20000;
    private long maxLifetime = 1800000;
}
