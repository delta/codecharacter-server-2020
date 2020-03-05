package delta.codecharacter.server.model;

import delta.codecharacter.server.util.enums.Status;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@Builder
public class CodeStatus {
    @Id
    @NotNull
    @Positive
    private Integer id;

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
    private Date lastSavedAt = new Date();

    @Field("read_only")
    @NotNull
    @Builder.Default
    private boolean readOnly = false;

    @Field("is_locked")
    @NotNull
    @Builder.Default
    private boolean isLocked = false;

    // null, if head is at latest
    @Field("current_commit")
    private String currentCommit;
}
