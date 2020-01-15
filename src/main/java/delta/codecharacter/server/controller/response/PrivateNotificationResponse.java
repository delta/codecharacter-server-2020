package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.Type;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class PrivateNotificationResponse {
    @NotNull
    @Positive
    private Integer notificationId;

    @NotNull
    @Positive
    private Integer userId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String title;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 1, max = 100)
    private String content;

    @NotNull
    private Type type;

    @NotNull
    @Builder.Default
    private Boolean isRead = false;
}
