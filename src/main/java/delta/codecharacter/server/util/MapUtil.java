package delta.codecharacter.server.util;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class MapUtil {

    @Value("storage/maps")
    private static String mapStoragePath;

    /**
     * Return the absolute path to the map directory
     *
     * @return Path to codes directory
     */
    public static String getMapRepositoryUri() {
        return System.getProperty("user.dir") + File.separator + mapStoragePath;
    }

    /**
     * Return the absolute path to the map file for the given mapId
     *
     * @param mapId MapId of the map to be accessed
     * @return Path to map file
     */
    public static String getMapFileUri(Integer mapId) {
        return getMapRepositoryUri() + File.separator + mapId;
    }

    /**
     * Get map of given mapId
     *
     * @param mapId MapId of the map
     * @return Contents of the map file
     */
    public static String getMap(Integer mapId) {
        String mapFileUri = getMapFileUri(mapId);
        if (!FileHandler.checkFileExists(getMapRepositoryUri())) return null;
        return FileHandler.getFileContents(mapFileUri);
    }
}
