package delta.codecharacter.server.controller.response.Leaderboard;

import delta.codecharacter.server.model.UserRating;
import delta.codecharacter.server.util.enums.Division;
import delta.codecharacter.server.util.enums.UserType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
public class PublicLeaderboardResponse {

    @Field("user_id")
    private Integer userId;

    private String username;

    private Integer wins;

    private Integer losses;

    private Integer ties;

    private Division division;

    private Integer rank;

    private List<UserRating> rating;

    private UserType userType;

    private String fullname;

    private String country;

    private Integer avatarId;
}
