package delta.codecharacter.server.util;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class MapFile {

    @Value("storage/maps")
    private static String mapStoragePath;

    public static String getMapRepositoryUri() {
        return System.getProperty("user.dir") + mapStoragePath;
    }

    public static String getMapFileUri(Integer mapId) {
        return getMapRepositoryUri() + File.separator + mapId;
    }

    public static String getMap(Integer mapId) {
        String mapFileUri = getMapFileUri(mapId);
        if (!FileHandler.checkFileExists(getMapRepositoryUri())) return null;
        return FileHandler.getFileContents(mapFileUri);
    }
}
