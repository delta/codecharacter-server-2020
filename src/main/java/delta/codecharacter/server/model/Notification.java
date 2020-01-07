package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class Notification {

    @Id
    @NotNull
    @Positive
    private Integer id;

    @NotNull
    @Positive
    private Integer userId;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @NotBlank(message = "Type is mandatory")
    private String type;
}
