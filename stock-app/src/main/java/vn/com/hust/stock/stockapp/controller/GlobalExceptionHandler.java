package vn.com.hust.stock.stockapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import vn.com.hust.stock.stockmodel.exception.BusinessErrorCode;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.exception.PermissionException;
import vn.com.hust.stock.stockmodel.login.ErrorResponse;
import vn.com.hust.stock.stockmodel.until.RequestLOG;


import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletionException;

@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ErrorResponse> beHandler(HttpServletRequest request, BusinessException ex) {
        return makeResponseError(request, ex.getErrorCode(), ex);
    }

    @ExceptionHandler(value = CompletionException.class)
    public ResponseEntity<ErrorResponse> ceHandler(HttpServletRequest request, Exception ex) {
        if (ex instanceof CompletionException && ex.getCause() instanceof BusinessException) {
            BusinessException cause = (BusinessException) ex.getCause();
            return makeResponseError(request, cause.getErrorCode(), cause);
        }
        return makeResponseError(request, ErrorCode.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> eHandler(HttpServletRequest request, Exception ex) {
        if (ex instanceof IllegalArgumentException || ex instanceof MissingServletRequestParameterException) {
            return makeResponseError(request, ErrorCode.JSON_PROCESSING_ERROR, ex);
        }
        return makeResponseError(request, ErrorCode.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(value = PermissionException.class)
    public ResponseEntity<ErrorResponse> pHandler(HttpServletRequest request, PermissionException ex) {
        ErrorResponse err = new ErrorResponse("FSGW-001", "Permission denied", "Permission denied");
        int httpStatus = 403;
        RequestLOG.error(request, httpStatus, err, ex);
        return new ResponseEntity<>(err, HttpStatus.valueOf(httpStatus));
    }


    private ResponseEntity<ErrorResponse> makeResponseError(HttpServletRequest request, BusinessErrorCode errorCode, Throwable ex) {
        ErrorResponse err = createErrorResponse(request, errorCode, ex);
        return new ResponseEntity<>(err, HttpStatus.valueOf(errorCode.getHttpStatus()));
    }

    private ErrorResponse createErrorResponse(HttpServletRequest request, BusinessErrorCode errorCode, Throwable ex) {
        ErrorResponse errResp;
        if (ex == null) {
            errResp = new ErrorResponse(errorCode.getCode(), errorCode.getDescription());
        } else {
            errResp = new ErrorResponse(errorCode.getCode(), errorCode.getDescription(), ex.getMessage());
        }
        log.error("request {}, code: {}, errResponse: {}",request, errorCode.getHttpStatus(), errResp, ex);
        return errResp;
    }
}

