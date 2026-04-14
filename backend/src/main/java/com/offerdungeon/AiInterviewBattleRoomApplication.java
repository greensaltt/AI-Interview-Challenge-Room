package com.offerdungeon;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@ConfigurationPropertiesScan
@SpringBootApplication
public class AiInterviewBattleRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInterviewBattleRoomApplication.class, args);
    }
}

