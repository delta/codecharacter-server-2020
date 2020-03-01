package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRatingsResponse {

    private Double rating;

    private Double ratingDeviation;

    private LocalDateTime validFrom;
}
