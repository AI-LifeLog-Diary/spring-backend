package com.lifelog.diary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.Async;

@EnableJpaAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
public class DiaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiaryApplication.class, args);
	}

}
