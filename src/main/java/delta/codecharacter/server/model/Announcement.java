package delta.codecharacter.server.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class Announcement {
    @Id
    @NotNull
    @Positive
    private Integer id;

    @Field("admin_user_id")
    @NotNull
    @Positive
    private Integer adminUserId;

    @NotNull
    private String message;

    @Field("created_at")
    @NotNull
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
