package com.free;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.free.aiknowledgehub.mapper")
public class AiKnowledgeHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeHubApplication.class, args);
    }

}
