package delta.codecharacter.server.controller.request.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicUserRequest {
    private String username;

    private String email;

    private String fullName;
}
