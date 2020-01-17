package delta.codecharacter.server.controller.exceptionhandler;

import delta.codecharacter.server.controller.response.ResponseError;
import delta.codecharacter.server.controller.response.ResponseException;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Logger;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger LOG = Logger.getLogger(ControllerExceptionHandler.class.getName());

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseError> handleMissingRequestBody(HttpMessageNotReadableException exception) {
        LOG.info("Exception: " + exception);
        ResponseError response = ResponseError.builder()
                .message("Invalid parameters in body")
                .status(400)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConversionException.class)
    public ResponseEntity<ResponseError> handleConversionException(ConversionException exception) {
        LOG.info("Conversion Exception: " + exception.getMessage() + ", localized message: " + exception.getLocalizedMessage());
        ResponseError response = ResponseError.builder()
                .message("Invalid parameters")
                .status(400)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<ResponseError> handleErrorResponse(ResponseException responseException) {
        LOG.info("Response Exception: " + responseException);
        ResponseError response = ResponseError.builder()
                .message(responseException.getMessage())
                .status(responseException.getStatusCode())
                .build();
        HttpStatus httpStatus = getHttpStatusFromCode(responseException.getStatusCode());
        return new ResponseEntity<>(response, httpStatus);
    }

    private HttpStatus getHttpStatusFromCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }
}
