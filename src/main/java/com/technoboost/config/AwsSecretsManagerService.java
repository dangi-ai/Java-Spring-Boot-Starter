package com.technoboost.config;

import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Slf4j
@Service
@Profile("prod")
public class AwsSecretsManagerService {

    private final AwsSecretProperties secretProperties;
    private final ObjectMapper objectMapper;

    private volatile DatabaseSecret cachedSecret;

    public AwsSecretsManagerService(AwsSecretProperties secretProperties, ObjectMapper objectMapper) {
        this.secretProperties = secretProperties;
        this.objectMapper = objectMapper;
    }

    public DatabaseSecret getDatabaseSecret() {
        if (cachedSecret != null) {
            return cachedSecret;
        }
        synchronized (this) {
            if (cachedSecret != null) {
                return cachedSecret;
            }
            cachedSecret = fetchSecret();
            return cachedSecret;
        }
    }

    public void evictCache() {
        synchronized (this) {
            cachedSecret = null;
        }
    }

    private DatabaseSecret fetchSecret() {
        log.info("Fetching database secret from AWS Secrets Manager: {}", secretProperties.getSecretName());

        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(secretProperties.getRegion()))
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretProperties.getSecretName())
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            String secretJson = response.secretString();

            DatabaseSecret secret = objectMapper.readValue(secretJson, DatabaseSecret.class);
            log.info("Successfully fetched database secret for host: {}", secret.getHost());
            return secret;

        } catch (SecretsManagerException e) {
            log.error("Failed to fetch secret '{}' from AWS Secrets Manager: {}",
                    secretProperties.getSecretName(), e.awsErrorDetails().errorMessage());
            throw new IllegalStateException("Cannot retrieve database credentials from AWS Secrets Manager", e);
        } catch (Exception e) {
            log.error("Failed to parse database secret: {}", e.getMessage());
            throw new IllegalStateException("Cannot parse database credentials from AWS Secrets Manager", e);
        }
    }
}
