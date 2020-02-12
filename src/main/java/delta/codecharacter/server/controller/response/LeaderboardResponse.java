package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.EnumFiles.Division;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class LeaderboardResponse {

    @Field("user_id")
    private Integer userId;

    private String username;

    private Integer rating;

    private Division division;
}
