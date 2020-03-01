package delta.codecharacter.server.controller.response.Leaderboard;

import delta.codecharacter.server.util.enums.Division;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
public class LeaderboardData {

    @Field("user_id")
    private Integer userId;

    private String username;

    private List<Integer> rating;

    private Division division;

    private Integer rank;
}
