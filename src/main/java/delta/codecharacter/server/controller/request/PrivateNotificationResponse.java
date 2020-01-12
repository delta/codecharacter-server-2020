package delta.codecharacter.server.controller.request;

import delta.codecharacter.server.utils.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString
public class PrivateNotificationResponse {
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
