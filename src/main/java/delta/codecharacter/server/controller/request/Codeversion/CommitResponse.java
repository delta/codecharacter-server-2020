package delta.codecharacter.server.controller.request.Codeversion;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CommitResponse {
    private String commitName;

    private String commitHash;

    private Date timestamp;
}
