package com.rephelper;// In your main application class (RephelperApplication)
import com.rephelper.infrastructure.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita o agendamento de tarefas// Add this line
@EnableConfigurationProperties(JwtProperties.class)
public class RephelperApplication {

	public static void main(String[] args) {
		SpringApplication.run(RephelperApplication.class, args);
	}
}