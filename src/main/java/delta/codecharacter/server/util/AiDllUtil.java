package delta.codecharacter.server.util;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class AiDllUtil {

    @Value("/storage/AI/dlls")
    private static String aiDllStoragePath;

    /**
     * Return the absolute path to the AI dll files directory of given aiId
     *
     * @param aiId - AiId of the ai
     * @return Path to codes directory
     */
    public static String getAiDllRepositoryUri(Integer aiId) {
        return System.getProperty("user.dir") + File.separator + aiDllStoragePath + File.separator + aiId;
    }

    /**
     * Return the absolute path to the player AI dll file of given aiId
     *
     * @param aiId - AiId of the ai
     * @return Path to required AI dll file
     */
    private static String getAiDllFileUri(Integer aiId) {
        return getAiDllRepositoryUri(aiId) + File.separator + "AI_DLL";
    }

    /**
     * @param aiId - AiId of the ai
     *             Create a new AiDll repository for given aiId
     */
    @SneakyThrows
    public static void createAiDllRepository(Integer aiId) {
        String aiDllRepositoryUri = getAiDllRepositoryUri(aiId);
        if (!FileHandler.checkFileExists(aiDllRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(aiDllRepositoryUri);
            if (!dirCreated) {
                throw new Exception("Cannot create directory");
            }
        }
    }

    /**
     * Get AIdll of given aiId
     *
     * @param aiId - AiId of the ai
     * @return Contents of the required dll file
     */
    public static String getAiDll(Integer aiId) {
        String aiDllFileUri = getAiDllFileUri(aiId);
        try {
            return FileHandler.getFileContents(aiDllFileUri);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set contents of dll file of given aiId
     *
     * @param aiId  - AiId of the ai
     * @param aiDll Contents of Aidll to be written in the AiDll file
     */
    public static void setAiDll(Integer aiId, String aiDll) {
        String aiDllFileUri = getAiDllFileUri(aiId);
        FileHandler.writeFileContents(aiDllFileUri, aiDll);
    }

    /**
     * Delete the dll file for the given aiId
     *
     * @param aiId - AiId of the ai
     */
    public static void deleteAiDllFile(Integer aiId) {
        FileHandler.deleteFile(getAiDllFileUri(aiId));
    }
}
