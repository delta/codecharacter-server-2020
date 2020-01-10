package delta.codecharacter.server.model;

import delta.codecharacter.server.util.Division;
import delta.codecharacter.server.util.Mode;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class Leaderboard {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Positive
    private Integer rating;

    @NotNull
    @Builder.Default
    private Division division;

    @Field("match_mode")
    @NotNull
    @Builder.Default
    private Mode matchMode;
}
