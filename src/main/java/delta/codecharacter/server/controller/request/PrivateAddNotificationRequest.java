package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import delta.codecharacter.server.util.Type;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivateAddNotificationRequest {

    @NotNull
    private Integer userId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String title;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String content;

    @NotNull
    private Type type;
}
