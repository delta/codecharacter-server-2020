package delta.codecharacter.server.util;

import delta.codecharacter.server.util.enums.DllId;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

public class DllUtil {

    @Value("storage/dlls")
    private static String dllStoragePath;

    /**
     * Return the absolute path to the dll files directory of given userId
     *
     * @param userId - UserId of the user whose directory is to be accessed
     * @return Path to codes directory
     */
    public static String getDllRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + File.separator + dllStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player dll file of given userId
     *
     * @param userId UserId of user whose dll is to be accessed
     * @param dllId  DllId of the dll needed
     * @return Path to required dll file
     */
    private static String getDllFileUri(Integer userId, DllId dllId) {
        return getDllRepositoryUri(userId) + File.separator + dllId;
    }

    /**
     * Create a new dll repository for given userId
     *
     * @param userId UserId of the user
     */
    @SneakyThrows
    public static void createDllRepository(Integer userId) {
        String dllRepositoryUri = getDllRepositoryUri(userId);
        if (!FileHandler.checkFileExists(dllRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(dllRepositoryUri);
            if (!dirCreated) {
                throw new Exception("Cannot create directory");
            }
        }
    }

    /**
     * Get dll of given userId
     *
     * @param userId UserId of user
     * @param dllId  DllId of the dll needed
     * @return Contents of the required dll file
     */
    public static String getDll(Integer userId, DllId dllId) {
        String dllFileUri = getDllFileUri(userId, dllId);
        if (!FileHandler.checkFileExists(dllFileUri)) return null;
        return FileHandler.getFileContents(dllFileUri);
    }

    /**
     * Set contents of dll file of given userId
     *
     * @param userId UserId of user
     * @param dllId  DllId of the dll which is to be set
     * @param dll    Contents of dll to be written in the dll file
     */
    public static void setDll(Integer userId, DllId dllId, String dll) {
        String dllFileUri = getDllFileUri(userId, dllId);
        FileHandler.writeFileContents(dllFileUri, dll);
    }

    /**
     * Delete the dll Directory
     *
     * @param userId UserId of user whose dll directory is to be deleted
     */
    @SneakyThrows
    public static void deleteDllDirectory(Integer userId) {
        if (!FileHandler.checkFileExists(getDllRepositoryUri(userId))) return;
        Path path = Path.of(getDllRepositoryUri(userId));
        boolean deleted = FileSystemUtils.deleteRecursively(path);
        if (!deleted) {
            throw new Exception("Directory cannot be deleted");
        }
    }
}
