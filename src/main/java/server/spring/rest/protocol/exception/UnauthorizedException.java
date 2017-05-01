package server.spring.rest.protocol.exception;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ilya Ivanov
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User is not authorized")
public class UnauthorizedException extends HttpException {
    public UnauthorizedException() {
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
