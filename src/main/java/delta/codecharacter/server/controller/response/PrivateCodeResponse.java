package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class PrivateCodeResponse {
    @NotNull
    private String code;
}
