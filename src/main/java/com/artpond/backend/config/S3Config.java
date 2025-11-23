package com.artpond.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class S3Config {
    
    @Value("${aws.accessKeyId}")
    private String accessKey;
    
    @Value("${aws.secretKey}")
    private String secretKey;
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.s3.endpoint:}")
    private String endpoint;
    
    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region);
        
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, region)
            );
            builder.withPathStyleAccessEnabled(true);
        }
        
        return builder.build();
    }
}
