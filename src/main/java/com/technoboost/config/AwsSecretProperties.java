package com.technoboost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@Profile("prod")
@ConfigurationProperties(prefix = "aws.secretsmanager")
public class AwsSecretProperties {

    private String secretName;
    private String region;
}
