package delta.codecharacter.server.controller.request.Simulation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompileCodeRequest {
    private String jobType;

    private Integer userId;

    private String secretKey;

    private String code;
}
