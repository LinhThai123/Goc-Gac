package com.ecommerce.gocgac.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bật cơ chế tác vụ theo lịch (@Scheduled) cho toàn ứng dụng.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
