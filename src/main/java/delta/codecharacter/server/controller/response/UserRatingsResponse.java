package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRatingsResponse {

    private Integer rating;

    private LocalDateTime validFrom;
}
