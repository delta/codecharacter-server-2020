package delta.codecharacter.server.controller.request.Match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameDetails {
    @NotNull
    private boolean success;

    private Integer gameId;

    private Integer mapId;

    private GameResult results;

    private String errorType;

    private Integer interestingness;

    private Integer points1;

    private Integer points2;

    private String player1LogCompressed;

    private String player2LogCompressed;
}
