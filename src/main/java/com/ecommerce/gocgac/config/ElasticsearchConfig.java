package com.ecommerce.gocgac.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Configuration class cho Elasticsearch
 * Spring Boot sẽ tự động cấu hình ElasticsearchClient dựa trên properties:
 * spring.elasticsearch.uris trong application.yml
 * 
 * Không cần extends AbstractElasticsearchConfiguration với Spring Boot 3.x
 * Spring Boot sẽ tự động tạo ElasticsearchClient bean
 */
@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ecommerce.gocgac.repository")
public class ElasticsearchConfig {
    
    // Spring Boot 3.x tự động cấu hình ElasticsearchClient
    // dựa trên spring.elasticsearch.uris trong application.yml
    // Không cần cấu hình thủ công
}

