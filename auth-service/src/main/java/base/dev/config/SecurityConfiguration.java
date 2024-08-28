package base.dev.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.util.BasicAuthHelper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import java.security.KeyManagementException;
import org.apache.http.impl.client.HttpClients;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private XKeycloakProperties xKeycloakProperties;

    @Autowired
    public void setXKeycloakProperties(XKeycloakProperties xKeycloakProperties) {
        this.xKeycloakProperties = xKeycloakProperties;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.oauth2Client().and()
                .oauth2Login()
                .tokenEndpoint()
                .and()
                .userInfoEndpoint();

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        http
                .authorizeHttpRequests()
                .requestMatchers("/unauthenticated", "/oauth2/**", "/login/**").permitAll()
                .anyRequest()
                .fullyAuthenticated()
                .and()
                .logout()
                .logoutSuccessUrl("http://localhost:8080/realms/external/protocol/openid-connect/logout?redirect_uri=http://localhost:8081/");

        return http.build();
    }

    @Bean("xkeycloak")
    public Keycloak keycloak() {
        return KeycloakBuilder.builder().serverUrl(xKeycloakProperties.getAuthServerUrl()).realm(xKeycloakProperties.getRealm())
                .clientId(xKeycloakProperties.getResource()).clientSecret(xKeycloakProperties.getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .scope("openid email profile roles offline_access")
                .resteasyClient(
                        new ResteasyClientBuilderImpl().connectionPoolSize(
                                xKeycloakProperties.getConnectionPoolSize()).build())
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnProperty(value = "bank.authorization.admin.full-access", havingValue = "true", matchIfMissing = false)
    public AuthzClient authzClient() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", xKeycloakProperties.getSecret());

        org.keycloak.authorization.client.Configuration configuration =
                new org.keycloak.authorization.client.Configuration(
                        xKeycloakProperties.getAuthServerUrl(), xKeycloakProperties.getRealm(),
                        xKeycloakProperties.getResource(), clientCredentials, this.httpClient());
        return AuthzClient.create(configuration);
    }

    @Bean
    @ConditionalOnProperty(value = "bank.authorization.admin.full-access", havingValue = "true", matchIfMissing = false)
    public Http httpKeycloak() {

        final AuthzClient authzClient = this.authzClient();
        final org.keycloak.authorization.client.Configuration config = authzClient
                .getConfiguration();
        final Http http = new Http(
                config,
                (
                        final Map<String, List<String>> requestParams,
                        final Map<String, String> requestHeaders) -> {
                    final String secret = (String) config.getCredentials().get("secret");
                    if (secret == null) {
                        throw new BeanCreationException("Client secret not provided.");
                    }
                    requestHeaders.put(
                            "Authorization",
                            BasicAuthHelper.createHeader(config.getResource(), secret));
                });
        http.setServerConfiguration(authzClient.getServerConfiguration());
        return http;
    }

    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }

        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {

        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(
                100);
        return poolingConnectionManager;
    }

    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(
                        6000)
                .setConnectTimeout(
                        6000)
                .setSocketTimeout(
                        10000)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .build();
    }

    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();

                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 1000000;
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
