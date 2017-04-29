package client;

import client.springwebsock.MyWebSocketHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketMessagingAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * @author Ilya Ivanov
 */
//@SpringBootApplication
//@EnableAutoConfiguration(exclude = {
//                DataSourceAutoConfiguration.class,
//                DataSourceTransactionManagerAutoConfiguration.class,
//                HibernateJpaAutoConfiguration.class,
//                WebSocketAutoConfiguration.class,
//                WebSocketMessagingAutoConfiguration.class,
//                WebServicesAutoConfiguration.class,
//                WebMvcAutoConfiguration.class,
//                WebClientAutoConfiguration.class})
@ImportResource("classpath:spring/client-context.xml")
@SpringBootConfiguration
//@EnableAutoConfiguration
@ComponentScan(
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = {TypeExcludeFilter.class}
        ), @ComponentScan.Filter(
                type = FilterType.CUSTOM,
                classes = {AutoConfigurationExcludeFilter.class}
        )}
)
public class Client implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Client.class);

    /** connection port */
    private int port;

    /** connection host */
    private String host;

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void run() {
        HttpGet request = new HttpGet();
        // add request header
        request.addHeader("UserEntity-Agent", USER_AGENT);
        request.addHeader("application/xml", ACCEPT);
        try {
            request.setURI(new URI("/persons"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println(request);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<Transport> transports = new ArrayList<>(2);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport());

        SockJsClient sockJsClient = new SockJsClient( transports);
        final ListenableFuture<WebSocketSession> handshake = sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://localhost:8080/archive");
        handshake.addCallback(new ListenableFutureCallback<WebSocketSession>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(WebSocketSession webSocketSession) {
                System.out.println(webSocketSession.getUri());
            }
        });
        handshake.get();
        System.out.println("Handshake done");
    }

//    public static void main(String[] args) {
//        SpringApplication.run(Client.class);
//    }
//
//    @Bean
//    public CommandLineRunner demo(Client client) {
//        return (args) -> {
//            client.run();
//            List<Transport> transports = new ArrayList<>(2);
//            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
//            transports.add(new RestTemplateXhrTransport());
//
//            SockJsClient sockJsClient = new SockJsClient(transports);
//            final ListenableFuture<WebSocketSession> handshake = sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://example.com:8080/archive");
//            handshake.addCallback(new ListenableFutureCallback<WebSocketSession>() {
//                @Override
//                public void onFailure(Throwable throwable) {
//
//                }
//
//                @Override
//                public void onSuccess(WebSocketSession webSocketSession) {
//                    System.out.println(webSocketSession.getHandshakeHeaders());
//                    System.out.println(webSocketSession.getId());
//                    System.out.println(webSocketSession.getUri());
//                }
//            });
//            sockJsClient.start();
//        };
//    }
}
