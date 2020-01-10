package delta.codecharacter.server.model;

import delta.codecharacter.server.utils.Type;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Field("is_read")
    @NotNull
    @Builder.Default
    private Boolean isRead = false;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @NotBlank(message = "Type is mandatory")
    private Type type;
}
