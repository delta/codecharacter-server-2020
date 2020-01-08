package delta.codecharacter.server.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

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
