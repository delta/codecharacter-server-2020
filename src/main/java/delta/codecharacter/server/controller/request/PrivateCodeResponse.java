package delta.codecharacter.server.controller.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
public class PrivateCodeResponse {
    @NotNull
    private String status;

    private String code;
}
