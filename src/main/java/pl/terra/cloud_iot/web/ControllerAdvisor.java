package pl.terra.cloud_iot.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import pl.terra.cloud_iot.exceptions.AlreadyExistException;
import pl.terra.cloud_iot.exceptions.ConflictException;
import pl.terra.common.exception.NotFoundException;

@ControllerAdvice
public class ControllerAdvisor {
    private static final Logger logger = LogManager.getLogger(ControllerAdvisor.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Void> handleNotFoundException(Exception ex, WebRequest request) {
        ControllerAdvisor.logger.error(String.format("caught NOT_FOUND exception: '%s'", ex.getMessage()));

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    ResponseEntity<String> handleAlreadyExistException(Exception ex, WebRequest request) {
        ControllerAdvisor.logger.error(String.format("caught AlreadyExistException exception: '%s'", ex.getMessage()));

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<String> handleConflictException(Exception ex, WebRequest request) {
        ControllerAdvisor.logger.error(String.format("caught AlreadyExistException exception: '%s'", ex.getMessage()));

        return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
