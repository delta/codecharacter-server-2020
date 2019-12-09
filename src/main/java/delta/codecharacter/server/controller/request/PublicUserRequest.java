package delta.codecharacter.server.controller.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicUserRequest {
    private String username;

    private String email;

    private String fullName;
}
