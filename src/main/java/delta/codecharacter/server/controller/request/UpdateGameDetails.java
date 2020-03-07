package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateGameDetails {
    private Boolean success;

    private Integer id;

    private String log;

    private Verdict verdict;

    private String winType;

    private Integer matchId;

    private Integer points1;

    private Integer points2;

    private String player1LogCompressed;

    private String player2LogCompressed;

    private String errorType;
}

