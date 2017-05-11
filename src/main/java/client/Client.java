package client;

import client.http.SocketRunner;
import client.view.View;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import server.spring.data.model.UserEntity;
import server.spring.rest.dispatcher.ResponseEntitySerializer;
import server.spring.rest.exception.HttpException;
import server.spring.rest.dispatcher.RequestEntitySerializer;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URI;

/**
 * @author Ilya Ivanov
 */
@ComponentScan(basePackages = {"client", "server.spring.rest.dispatcher", "server.spring.rest.parsers"})
@ImportResource("classpath:spring/network-context.xml")
public class Client {
    /** connection port */
    private Integer port;

    /** connection host */
    private String host;

    @Autowired
    public Client(Integer port, String host) {
        this.port = port;
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(Client.class);
    }
}
