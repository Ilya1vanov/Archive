package server.spring.rest.dispatcher;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import server.spring.rest.dispatcher.serializer.RequestEntitySerializer;
import server.spring.rest.dispatcher.serializer.ResponseEntitySerializer;
import server.spring.rest.mapping.HandlerMapping;
import server.spring.rest.exception.UnprocessableEntityException;
import server.spring.rest.session.SessionManager;

import java.io.*;
import java.net.Socket;

/**
 * @author Ilya Ivanov
 */
@Component
@Scope("prototype")
@Lazy
public class SocketDispatcher implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(SocketDispatcher.class);

    /** server-side socket */
    private Socket self;

    @Autowired private ApplicationContext context;

    @Autowired private HandlerMapping handlerMapping;

    @Autowired private SessionManager sessionManager;

    @Autowired
    public SocketDispatcher(Socket self) {
        this.self = self;
    }

    /**
     * Main request-response cycle on server-side.
     */
    @Override
    public void run() {
        try (final ObjectInputStream in = new ObjectInputStream(self.getInputStream());
             final ObjectOutputStream out = new ObjectOutputStream(self.getOutputStream())) {

            ResponseEntity response;
            try {
                RequestEntitySerializer requestSerializer;
                try {
                    requestSerializer = (RequestEntitySerializer) in.readObject();
                    requestSerializer.setApplicationContext(context);
                    log.debug(requestSerializer);
                } catch (ClassNotFoundException e) {
                    throw new UnprocessableEntityException("Unknown request type: " + e.getMessage());
                } finally {
                    self.shutdownInput();
                }

                final RequestEntity request = requestSerializer.getEntity();
                sessionManager.authorize(request);

                response = handlerMapping.handle(request);
            } catch (Throwable e) {
                final ResponseStatus annotation = e.getClass().getAnnotation(ResponseStatus.class);
                if (annotation != null) {
                    final HttpStatus code = annotation.value();
                    final String reason = e.getMessage();
                    response =  ResponseEntity.status(code).body(reason);
                } else {
                    log.error(e);
                    e.printStackTrace();
                    response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown error");
                }
            }

            final ResponseEntitySerializer writer = context.getBean(ResponseEntitySerializer.class, response);
            out.writeObject(writer);
            log.debug(response);
            self.shutdownOutput();
        } catch (IOException e) {
            log.warn("Connection is lost: ", e);
        }
    }
}
