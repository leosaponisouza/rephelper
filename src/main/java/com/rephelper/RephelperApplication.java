package com.rephelper;// In your main application class (RephelperApplication)
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.rephelper.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class) // Add this line
public class RephelperApplication {

	public static void main(String[] args) {
		SpringApplication.run(RephelperApplication.class, args);
	}
}