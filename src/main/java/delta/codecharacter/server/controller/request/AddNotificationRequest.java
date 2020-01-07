package delta.codecharacter.server.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddNotificationRequest {
    @NotNull
    private Integer userId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String title;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 1, max = 100)
    private String content;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @Length(min = 5, max = 50)
    private String type;
}
