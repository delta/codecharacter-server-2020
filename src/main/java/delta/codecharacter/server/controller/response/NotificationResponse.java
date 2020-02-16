package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.enums.Type;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class NotificationResponse {
    @NotNull
    @Positive
    private Integer notificationId;

    @NotNull
    @Positive
    private Integer userId;

    private String title;

    private String content;

    @NotNull
    private Type type;

    @NotNull
    private Boolean isRead;
}
