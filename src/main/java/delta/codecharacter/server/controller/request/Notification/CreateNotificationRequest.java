package delta.codecharacter.server.controller.request.Notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import delta.codecharacter.server.util.enums.Type;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateNotificationRequest {

    @NotNull
    private Integer userId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String title;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 100)
    private String content;

    @NotNull
    private Type type;
}
