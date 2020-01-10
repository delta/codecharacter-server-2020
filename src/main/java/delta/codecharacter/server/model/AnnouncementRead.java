package delta.codecharacter.server.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class AnnouncementRead {
    @Field("user_id")
    @NotNull
    @Positive
    private Integer userId;

    @Field("announcement_id")
    @NotNull
    @Positive
    private Integer announcementId;

    @Field("is_read")
    @NotNull
    @Builder.Default
    private Boolean isRead = false;

    @Field("is_deleted")
    @NotNull
    @Builder.Default
    private Boolean isDeleted = false;
}
