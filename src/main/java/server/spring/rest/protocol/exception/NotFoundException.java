package server.spring.rest.protocol.exception;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ilya Ivanov
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Cannot find this record")
public class NotFoundException extends HttpException {
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }
}
