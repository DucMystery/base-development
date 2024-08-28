package base.dev.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth.keycloak")
public class XKeycloakProperties {
    private String realm;

    private String resource;

    private String authServerUrl;

    private int connectionPoolSize= 20;

    private String secret;
}
