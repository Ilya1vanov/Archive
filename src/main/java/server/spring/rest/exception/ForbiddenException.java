package server.spring.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ilya Ivanov
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Permission denied")
public class ForbiddenException extends HttpException {
    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
