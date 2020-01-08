package delta.codecharacter.server.model;

import lombok.*;

import org.springframework.data.annotation.Id;

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

    @NotNull
    @Positive
    private Integer matchId;

    @NotNull
    private String status;

    @NotNull
    private String verdict;

    @NotNull
    private Integer interestingness;

    @NotNull
    private Integer points1;

    @NotNull
    private Integer points2;

    @NotNull
    @Positive
    private Integer mapId;
}
