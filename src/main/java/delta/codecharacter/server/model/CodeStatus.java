package delta.codecharacter.server.model;

import delta.codecharacter.server.util.Status;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class CodeStatus {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Builder.Default
    private Status status = Status.IDLE;

    @Field("last_saved_at")
    @NotNull
    @Builder.Default
    private LocalDateTime lastSavedAt = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
}
