package com.example.tasktelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskTelegramBotApplication.class, args);
    }

}
