package delta.codecharacter.server.model;

import delta.codecharacter.server.util.enums.Status;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@Builder
public class Game {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @Field("match_id")
    @NotNull
    @Positive
    private Integer matchId;

    @NotNull
    @Builder.Default
    private Status status = Status.IDLE;

    @NotNull
    @Builder.Default
    private Verdict verdict = Verdict.TIE;

    @Field("points_1")
    @Builder.Default
    private Integer points1 = 0;

    @Field("points_2")
    @Builder.Default
    private Integer points2 = 0;

    @Field("map_id")
    @NotNull
    @Positive
    private Integer mapId;

    @Field("created_at")
    @NotNull
    @Builder.Default
    private Date createdAt = new Date();
}
