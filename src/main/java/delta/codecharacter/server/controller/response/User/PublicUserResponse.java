package delta.codecharacter.server.controller.response.User;

import delta.codecharacter.server.util.enums.UserType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PublicUserResponse {

    private Integer userId;

    private String username;

    private String fullName;

    private UserType userType;

    private String country;

    private String college;

    private Integer avatarId;

    private Date createdAt;
}
