package com.mehmetkerem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class AppConfig {

    @Bean
    public TransactionTemplate transactionTemplate(@NonNull PlatformTransactionManager txManager) {
        return new TransactionTemplate(txManager);
    }
}
