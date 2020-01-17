package delta.codecharacter.server.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PaginationUtil {
    public static <T> List<T> getPaginatedList(@NotNull List<T> entries, @NotNull @Positive Integer pageNumber, @NotNull @Positive Integer size) {
        List<T> paginatedEntries = new ArrayList<>();

        int totalNumberEntries = entries.size();
        int start = ((pageNumber - 1) * size);

        if (start < size) {
            int end = Math.min(start + size, totalNumberEntries - start);
            paginatedEntries = entries.subList(start, end);
        }

        return paginatedEntries;
    }

    public static <T> Integer getTotalPages(@NotNull List<T> entries, @NotNull @Positive Integer size) {
        return entries.size() / size + 1;
    }
}
