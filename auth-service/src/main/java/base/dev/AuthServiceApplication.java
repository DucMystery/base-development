package base.dev;

import base.dev.config.XKeycloakProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication(scanBasePackages = "base.dev")
@EnableConfigurationProperties(XKeycloakProperties.class)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("Hello world!");
    }
}