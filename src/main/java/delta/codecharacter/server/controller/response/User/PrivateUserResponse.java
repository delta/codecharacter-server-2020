package delta.codecharacter.server.controller.response.User;

import delta.codecharacter.server.util.enums.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateUserResponse {

    private Integer userId;

    private String username;

    private String fullName;

    private UserType userType;

    private String email;

    private String country;

    private String college;

    private Boolean isAdmin;

    private int avatarId;

    private Boolean isFirstLogin;

    private Integer currentLevel;
}
