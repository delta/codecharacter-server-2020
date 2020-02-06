package delta.codecharacter.server.controller.response;

import delta.codecharacter.server.util.Type;
import lombok.Builder;
import lombok.Data;

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

    private String title;

    private String content;

    @NotNull
    private Type type;

    @NotNull
    @Builder.Default
    private Boolean isRead = false;
}
