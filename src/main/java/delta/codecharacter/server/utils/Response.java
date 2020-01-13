package delta.codecharacter.server.utils;

import lombok.*;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Response<T> {
    @NotNull
    @Builder.Default
    private Integer status = 200;
    private T response;

    public HttpStatus getHttpStatus() {
        return HttpStatus.resolve(status);
    }

    public static HttpStatus getHttpStatus(int httpStatusCode) {
        return HttpStatus.resolve(httpStatusCode);
    }
}
