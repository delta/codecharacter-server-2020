package delta.codecharacter.server.controller.request.User;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ActivateUserRequest {

    @NotNull
    private String authToken;

    @NotNull
    private Integer userId;
}
