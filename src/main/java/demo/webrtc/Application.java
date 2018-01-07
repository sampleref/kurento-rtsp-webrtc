package demo.webrtc;


import demo.webrtc.app.WebrtcChannelHandler;
import demo.webrtc.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@EnableWebSocket
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, CassandraDataAutoConfiguration.class})
public class Application implements WebSocketConfigurer {

    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
    static final String DEFAULT_APP_SERVER_URL = "https://localhost:8443";

    @Bean
    public WebrtcChannelHandler handler() {
        return new WebrtcChannelHandler();
    }

    @Bean
    public KurentoClient kurentoClient() {
        return Constants.getKurentoClient();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(handler(), "/player").setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                kill();
            }
        });
        new SpringApplication(Application.class).run(args);
    }

    public static void kill() {
        LOG.error("Stopping the service. JVM is shutting down");
    }

}
