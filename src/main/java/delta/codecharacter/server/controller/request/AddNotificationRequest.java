package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import delta.codecharacter.server.utils.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddNotificationRequest {

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String title;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 1, max = 100)
    private String content;

    @NotNull
    private Type type;

    private Boolean isRead = false;
}
