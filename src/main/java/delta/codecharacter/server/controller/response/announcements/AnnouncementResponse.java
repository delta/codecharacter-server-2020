package delta.codecharacter.server.controller.response.announcements;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnouncementResponse {
    private Integer id;

    private String message;
}

