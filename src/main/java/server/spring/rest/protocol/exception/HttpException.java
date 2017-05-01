package server.spring.rest.protocol.exception;

import org.apache.log4j.Logger;

/**
 * @author Ilya Ivanov
 */
public class HttpException extends Exception {
    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }
}
