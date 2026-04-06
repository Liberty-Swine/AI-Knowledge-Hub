package com.free;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiKnowledgeHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeHubApplication.class, args);
    }

}
