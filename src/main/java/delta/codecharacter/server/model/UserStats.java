package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@Builder
public class UserStats {
    @NotNull
    @Positive
    @Field("user_id")
    private Integer userId;

    @NotNull
    @Field("total_matches")
    private Integer totalMatches;

    @NotNull
    @Field("total_wins")
    private Integer totalWins;

    @NotNull
    @Field("total_loses")
    private Integer totalLoses;


    @Field("last_match_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastMatchAt;
}
