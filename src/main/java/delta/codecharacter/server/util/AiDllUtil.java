package delta.codecharacter.server.util;

import delta.codecharacter.server.util.enums.AiDllId;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class AiDllUtil {

    @Value("storage/AI/dlls")
    private static String aiDllStoragePath;

    /**
     * Return the absolute path to the dll files directory of given userId
     *
     * @param userId - UserId of the user whose directory is to be accessed
     * @return Path to codes directory
     */
    public static String getAiDllRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + File.separator + aiDllStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player dll file of given userId
     *
     * @param userId  UserId of user whose dll is to be accessed dllStoragePath
     * @param aiDllId DllId of the dll needed
     * @return Path to required dll file
     */
    private static String getAiDllFileUri(Integer userId, AiDllId aiDllId) {
        return getAiDllRepositoryUri(userId) + File.separator + aiDllId;
    }

    /**
     * Create a new dll repository for given userId
     *
     * @param userId UserId of the user
     */
    @SneakyThrows
    public static void createAiDllRepository(Integer userId) {
        String aiDllRepositoryUri = getAiDllRepositoryUri(userId);
        if (!FileHandler.checkFileExists(aiDllRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(aiDllRepositoryUri);
            if (!dirCreated) {
                throw new Exception("Cannot create directory");
            }
        }
    }

    /**
     * Get dll of given userId
     *
     * @param userId  UserId of user
     * @param aiDllId DllId of the dll needed
     * @return Contents of the required dll file
     */
    public static String getAiDll(Integer userId, AiDllId aiDllId) {
        String aiDllFileUri = getAiDllFileUri(userId, aiDllId);
        try {
            return FileHandler.getFileContents(aiDllFileUri);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set contents of dll file of given userId
     *
     * @param userId  UserId of user
     * @param aiDllId DllId of the dll which is to be set
     * @param aiDll   Contents of dll to be written in the dll file
     */
    public static void setAiDll(Integer userId, AiDllId aiDllId, String aiDll) {
        String aiDllFileUri = getAiDllFileUri(userId, aiDllId);
        FileHandler.writeFileContents(aiDllFileUri, aiDll);
    }

    /**
     * Delete the dll file for the given userId and dllId
     *
     * @param userId  UserId of user whose dll directory is to be deleted
     * @param aiDllId DllId of the file to be deleted
     */
    public static void deleteAiDllFile(Integer userId, AiDllId aiDllId) {
        FileHandler.deleteFile(getAiDllFileUri(userId, aiDllId));
    }
}
