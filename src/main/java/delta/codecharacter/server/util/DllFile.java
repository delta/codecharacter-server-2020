package delta.codecharacter.server.util;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

public class DllFile {

    @Value("storage/dlls")
    private static String dllStoragePath;

    @Value("dll1")
    private static String dll1FileName;

    @Value("dll2")
    private static String dll2FileName;

    public static String getDllRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + dllStoragePath + File.separator + userId;
    }

    private static String getDll1FileUri(Integer userId) {
        return getDllRepositoryUri(userId) + File.separator + dll1FileName;
    }

    private static String getDll2FileUri(Integer userId) {
        return getDllRepositoryUri(userId) + File.separator + dll2FileName;
    }

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

    public static String getDll1(Integer userId) {
        String dll1FileUri = getDll1FileUri(userId);
        if (!FileHandler.checkFileExists(dll1FileUri)) return null;
        return FileHandler.getFileContents(dll1FileUri);
    }

    public static String getDll2(Integer userId) {
        String dll2FileUri = getDll2FileUri(userId);
        if (!FileHandler.checkFileExists(dll2FileUri)) return null;
        return FileHandler.getFileContents(dll2FileUri);
    }

    public static void setDll1(Integer userId, String dll) {
        String dll1FileUri = getDll1FileUri(userId);
        FileHandler.writeFileContents(dll1FileUri, dll);
    }

    public static void setDll2(Integer userId, String dll) {
        String dll2FileUri = getDll2FileUri(userId);
        FileHandler.writeFileContents(dll2FileUri, dll);
    }

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
