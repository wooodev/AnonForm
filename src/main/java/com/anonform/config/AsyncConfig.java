package com.anonform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // TODO: ThreadPoolTaskExecutor 설정 (응답 알림, CSV 생성 등 비동기 작업용)
}
