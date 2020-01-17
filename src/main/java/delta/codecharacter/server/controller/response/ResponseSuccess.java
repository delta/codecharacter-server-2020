package delta.codecharacter.server.controller.response;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseSuccess {
    @NotNull
    @Builder.Default
    private String message = "Success";

    @NotNull
    @Builder.Default
    private Integer status = 200;
}
