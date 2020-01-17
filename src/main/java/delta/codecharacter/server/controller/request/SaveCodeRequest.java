package delta.codecharacter.server.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class SaveCodeRequest {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private String code;
}
