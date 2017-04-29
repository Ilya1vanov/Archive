package server;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import server.springdata.model.UserEntity;
import server.springdata.repository.UserEntityRepository;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@ImportResource("classpath:spring/server-context.xml")
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Server implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Server.class);

    /** connection port */
    private int port;

    /** requested maximum length of the queue of incoming connections */
    private int backlog;

    public Server(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        log.info("Server run");
    }

    public static void main(String[] args) {
        final ConfigurableApplicationContext context =
                SpringApplication.run(Server.class);
    }

    @Bean
    public CommandLineRunner application(Server server, UserEntityRepository repository) {
        return (args) -> {
            repository.save(new UserEntity("sdkjjf", "sdfa"));
            final List<UserEntity> all = repository.findAll();
            log.debug(all);
            server.run();



        };
    }
}
