package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMatchRequest {
    private Integer matchId;

    private String secretKey;

    private Boolean success;

    private List<UpdateGameDetails> gameResults;

    private List<String> player1DLLs;

    private List<String> player2DLLs;

    private String error;

    private String errorType;
}
