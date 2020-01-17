package delta.codecharacter.server.controller.response;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseError {
    @NotNull
    @Builder.Default
    private String message = "Internal Server Error";

    @NotNull
    @Builder.Default
    private Integer status = 500;
}
