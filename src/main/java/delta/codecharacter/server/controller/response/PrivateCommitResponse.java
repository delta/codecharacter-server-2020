package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@Builder
public class PrivateCommitResponse {
    @NotNull
    private String commitHash;

    @NotNull
    private String commitMessage;

    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private LocalDateTime committedAt;
}
