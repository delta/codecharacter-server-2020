package delta.codecharacter.server.util;

import lombok.SneakyThrows;

public class PageUtils {
    /**
     * Validate values of page number and page size
     *
     * @param pageNumber page number
     * @param pageSize   page size
     */
    @SneakyThrows
    public static void validatePaginationParams(Integer pageNumber, Integer pageSize) {
        if (pageNumber < 1) {
            throw new Exception("Page number should not be less than 1");
        }
        if (pageSize < 1) {
            throw new Exception("Page size should not be less than 1");
        }
        if (pageSize > 100) {
            throw new Exception("Page size should not be greater than 100");
        }
    }
}
