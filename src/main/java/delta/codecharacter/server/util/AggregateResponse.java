package delta.codecharacter.server.util;

import delta.codecharacter.server.util.Enums.MatchMode;
import delta.codecharacter.server.util.Enums.Verdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AggregateResponse {
    private Long total;

    private Verdict verdict;

    private MatchMode matchMode;
}
