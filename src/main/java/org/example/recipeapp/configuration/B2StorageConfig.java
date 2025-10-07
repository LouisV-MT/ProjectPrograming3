package org.example.recipeapp.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class B2StorageConfig {
    @Value("${b2.access.key.id}")
    private String accessKeyId;

    @Value("${b2.secret.access.key}")
    private String secretAccessKey;

    @Value("${b2.endpoint}")
    private String endpoint;

    @Value("${b2.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create("https://" + endpoint ))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
