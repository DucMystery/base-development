package base.dev.common;

import base.dev.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(UnAuthException.class)
    public ResponseEntity<Object> handleUnAuthException(UnAuthException exception, WebRequest request){
        String message = exception.getMessage();
        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException exception, WebRequest request){
        String message = exception.getMessage();
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request){
        String message = exception.getMessage();
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<Object> handleInternalServiceException(InternalServiceException exception, WebRequest request){
        String message = exception.getMessage();
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException exception, WebRequest request){
        String message = exception.getMessage();
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
