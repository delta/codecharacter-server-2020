package delta.codecharacter.server.model;

<<<<<<< HEAD
=======
import lombok.*;

>>>>>>> Add model CodeStatus
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CodeStatus {
    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    private String latestSrcPath;

    @NotNull
    private String status;

    @NotNull
    private Date lastSavedAt;

    @NotNull
    private Date lastCompiledAt;
}
