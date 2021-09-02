package vn.com.hust.stock.stockmodel.exception;


import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.com.hust.stock.stockmodel.login.ErrorResponse;

import javax.servlet.http.HttpServletRequest;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(new ErrorResponse("400", "Malformed JSON request", ex.getMessage()));
    }

    @ExceptionHandler(DataAlreadyExistsException.class)
    protected ResponseEntity<Object> handleDuplicateFile(DataAlreadyExistsException ex) {
        ErrorResponse ErrorResponse = new ErrorResponse("400", "Duplicate file upload", ex.getMessage());
        return buildResponseEntity(ErrorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleTooLargeFile(MaxUploadSizeExceededException ex) {
        ErrorResponse ErrorResponse = new ErrorResponse("400", "File too large", ex.getMessage());
        return buildResponseEntity(ErrorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> defaultErrorHandler(HttpServletRequest req, Exception e)
            throws Exception {
        logger.error("Exception", e);
        if (AnnotationUtils.findAnnotation
                (e.getClass(), ResponseStatus.class) != null) {
            throw e;
        }

        ErrorResponse ErrorResponse = new ErrorResponse("500", e.getMessage());
        return buildResponseEntity(ErrorResponse);
    }


    private ResponseEntity<Object> buildResponseEntity(ErrorResponse errorResponse) {
        return ResponseEntity.badRequest().body(errorResponse);
    }


}