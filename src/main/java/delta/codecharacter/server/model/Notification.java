package delta.codecharacter.server.model;

import delta.codecharacter.server.util.enums.Type;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
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
    @Builder.Default
    private Type type = Type.INFO;

    @NotNull
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
