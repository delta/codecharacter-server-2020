package delta.codecharacter.server.controller.response.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicUserResponse {

    private Integer userId;

    private String username;

    private String country;

    private int avatarId;

}
