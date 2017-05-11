package server.spring.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ilya Ivanov
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error in HTTP headers")
public class BadRequestException extends HttpException {
    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }
}
