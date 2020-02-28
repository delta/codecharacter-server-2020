package delta.codecharacter.server.controller.request.User;

import delta.codecharacter.server.util.enums.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicUserRequest {

    private Integer userId;

    private String username;

    private String fullName;

    private UserType userType;

    private String email;

    private String country;

    private String college;

    private Boolean isAdmin;

    private int avatarId;
}
