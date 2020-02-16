package delta.codecharacter.server.util;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

public class DllUtil {

    @Value("{storage.playerdll.dir}")
    private static String dllStoragePath;

    @Value("dll1")
    private static String dll1FileName;

    @Value("dll2")
    private static String dll2FileName;

    /**
     * Return the absolute path to the dll files directory of given userId
     *
     * @param userId - UserId of whose directory is to be accessed
     * @return Path to codes directory
     */
    public static String getDllRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + dllStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player dll1 file of given userId
     *
     * @param userId UserId of whose dll1 is to be accessed
     * @return Path to player dll1 file
     */
    private static String getDll1FileUri(Integer userId) {
        return getDllRepositoryUri(userId) + File.separator + dll1FileName;
    }

    /**
     * Return the absolute path to the player dll2 file of given userId
     *
     * @param userId UserId of whose dll2 is to be accessed
     * @return Path to player dll2 file
     */
    private static String getDll2FileUri(Integer userId) {
        return getDllRepositoryUri(userId) + File.separator + dll2FileName;
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
     * Get dll1 of given userId
     *
     * @param userId UserId of user
     * @return Contents of dll1 file
     */
    public static String getDll1(Integer userId) {
        String dll1FileUri = getDll1FileUri(userId);
        if (!FileHandler.checkFileExists(dll1FileUri)) return null;
        return FileHandler.getFileContents(dll1FileUri);
    }

    /**
     * Get dll2 of given userId
     *
     * @param userId UserId of user
     * @return Contents of dll2 file
     */
    public static String getDll2(Integer userId) {
        String dll2FileUri = getDll2FileUri(userId);
        if (!FileHandler.checkFileExists(dll2FileUri)) return null;
        return FileHandler.getFileContents(dll2FileUri);
    }

    /**
     * Set contents of dll1 file of given userId
     *
     * @param userId UserId of user
     * @param dll    Contents of dll to be inside the dll1 file
     */
    public static void setDll1(Integer userId, String dll) {
        String dll1FileUri = getDll1FileUri(userId);
        FileHandler.writeFileContents(dll1FileUri, dll);
    }


    /**
     * Set contents of dll2 file of given userId
     *
     * @param userId UserId of user
     * @param dll    Contents of dll to be inside the dll2 file
     */
    public static void setDll2(Integer userId, String dll) {
        String dll2FileUri = getDll2FileUri(userId);
        FileHandler.writeFileContents(dll2FileUri, dll);
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
