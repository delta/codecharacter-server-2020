package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class PublicAnnouncementResponse {
    @NotNull
    @Positive
    private Integer announcementId;

    @NotNull
    @Positive
    private Integer adminUserId;

    @NotNull
    private String message;
}
