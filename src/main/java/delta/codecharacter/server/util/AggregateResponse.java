package delta.codecharacter.server.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AggregateResponse {
    private Long total;

    private Verdict verdict;

    private MatchMode matchMode;
}
