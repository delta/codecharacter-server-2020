package delta.codecharacter.server.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateMatchRequest {
    private Integer matchId;

    private String secretKey;

    private Boolean success;

    private List<UpdateGameDetails> gameResults;

    private List<String> player1DLLs;

    private List<String> player2Dlls;

    private String error;

    private String errorType;
}
