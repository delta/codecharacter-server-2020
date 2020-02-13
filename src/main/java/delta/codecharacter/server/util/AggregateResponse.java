package delta.codecharacter.server.util;

import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AggregateResponse {
    private Long total;

    private Verdict verdict;

    private MatchMode matchMode;
}
