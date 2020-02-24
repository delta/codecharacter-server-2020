package delta.codecharacter.server.util;

import delta.codecharacter.server.util.enums.DllId;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class DllUtil {

    @Value("storage/dlls")
    private static String dllStoragePath;

    /**
     * Return the absolute path to the dll files directory of given userId
     *
     * @param directory directory to get dll from
     * @param userId    UserId of the user whose directory is to be accessed
     * @return Path to codes directory
     */
    public static String getDllRepositoryUri(String directory, Integer userId) {
        //return System.getProperty("user.dir") + File.separator + dllStoragePath + File.separator + userId;
        return System.getProperty(directory) + File.separator + dllStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player dll file of given userId
     *
     * @param directory directory to get dll from
     * @param userId    UserId of user whose dll is to be accessed
     * @param dllId     DllId of the dll needed
     * @return Path to required dll file
     */
    private static String getDllFileUri(String directory, Integer userId, DllId dllId) {
        return getDllRepositoryUri(directory, userId) + File.separator + dllId;
    }

    /**
     * Create a new dll repository for given userId
     *
     * @param directory directory to get dll from
     * @param userId    UserId of the user
     */
    @SneakyThrows
    public static void createDllRepository(String directory, Integer userId) {
        String dllRepositoryUri = getDllRepositoryUri(directory, userId);
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
     * @param directory directory to get dll from
     * @param userId    UserId of user
     * @param dllId     DllId of the dll needed
     * @return Contents of the required dll file
     */
    public static String getDll(String directory, Integer userId, DllId dllId) {
        String dllFileUri = getDllFileUri(directory, userId, dllId);
        try {
            return FileHandler.getFileContents(dllFileUri);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set contents of dll file of given userId
     *
     * @param directory directory to get dll from
     * @param userId    UserId of user
     * @param dllId     DllId of the dll which is to be set
     * @param dll       Contents of dll to be written in the dll file
     */
    public static void setDll(String directory, Integer userId, DllId dllId, String dll) {
        String dllFileUri = getDllFileUri(directory, userId, dllId);
        FileHandler.writeFileContents(dllFileUri, dll);
    }

    /**
     * Delete the dll file for the given userId and dllId
     *
     * @param directory directory to get dll from
     * @param userId    UserId of user whose dll directory is to be deleted
     * @param dllId     DllId of the file to be deleted
     */
    public static void deleteDllFile(String directory, Integer userId, DllId dllId) {
        FileHandler.deleteFile(getDllFileUri(directory, userId, dllId));
    }
}
