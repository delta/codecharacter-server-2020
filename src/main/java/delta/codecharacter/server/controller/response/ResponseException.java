package delta.codecharacter.server.controller.response;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseException extends Throwable {
    @NotNull
    private String message;

    @NotNull
    private Integer statusCode;

    public static ResponseException getUnauthorizedResponseException(String message) {
        return ResponseException.builder()
                .message(message)
                .statusCode(401)
                .build();
    }

    public static ResponseException getNotFoundResponseException(String message) {
        return ResponseException.builder()
                .message(message)
                .statusCode(404)
                .build();
    }

    public static ResponseException getBadRequestException(String message) {
        return ResponseException.builder()
                .message(message)
                .statusCode(400)
                .build();
    }

    public static ResponseException getInternalServerErrorException(String message) {
        return ResponseException.builder()
                .message(message)
                .statusCode(500)
                .build();
    }

    public static ResponseException getUnauthorizedResponseException() {
        return getUnauthorizedResponseException("Unauthorized");
    }

    public static ResponseException getNotFoundResponseException() {
        return getNotFoundResponseException("Not found");
    }

    public static ResponseException getBadRequestException() {
        return getBadRequestException("Bad Request");
    }

    public static ResponseException getInternalServerErrorException() {
        return getInternalServerErrorException("Internal Server Error");
    }
}
