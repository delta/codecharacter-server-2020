package delta.codecharacter.server.model;

import delta.codecharacter.server.utils.Status;
import delta.codecharacter.server.utils.Verdict;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
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

    @NotNull
    @Builder.Default
    private Integer interestingness = 0;

    @Field("points_1")
    @NotNull
    private Integer points1;

    @Field("points_2")
    @NotNull
    private Integer points2;

    @Field("map_id")
    @NotNull
    @Positive
    private Integer mapId;
}
