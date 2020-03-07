package delta.codecharacter.server.controller.request;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Data
@JsonIgnoreProperties(value = "true")
public class LockCodeRequest {
    private String secretKey;

    private Integer userId;

    private Boolean success;

    private List<String> playerDlls;

    private String errorType;

    private String error;
}
