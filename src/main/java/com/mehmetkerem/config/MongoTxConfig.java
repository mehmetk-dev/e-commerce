package com.mehmetkerem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoTxConfig {

    @Bean
    MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory databaseFactory){
        return new MongoTransactionManager(databaseFactory);
    }
}
