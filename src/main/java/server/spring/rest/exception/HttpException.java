package server.spring.rest.exception;

/**
 * @author Ilya Ivanov
 */
public class HttpException extends Exception {
    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
