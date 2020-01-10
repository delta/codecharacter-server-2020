package delta.codecharacter.server.model;

import delta.codecharacter.server.utils.Status;
import delta.codecharacter.server.utils.Verdict;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * A match between two players. It may consist of more than one game.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Match {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @Field("player_id_1")
    @NotNull
    @Positive
    private Integer playerId1;

    @Field("player_id_2")
    @NotNull
    @Positive
    private Integer playerId2;

    @NotNull
    @Builder.Default
    private Verdict verdict = Verdict.TIE;

    @NotNull
    @Builder.Default
    private Status status = Status.IDLE;

    @Field("score_1")
    @NotNull
    private Integer score1;

    @Field("score_2")
    @NotNull
    private Integer score2;
}
