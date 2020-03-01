package delta.codecharacter.server.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserRatingsResponse {

    private Double rating;

    private Double ratingDeviation;

    private Date validFrom;
}
