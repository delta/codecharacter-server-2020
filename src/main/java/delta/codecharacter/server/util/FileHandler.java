package delta.codecharacter.server.util;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler {

    /**
     * Creates a new directory
     *
     * @param directoryUri Name of the directory
     * @return true, if created successfully. Else, false.
     */
    public static boolean createDirectory(String directoryUri) {
        if (checkFileExists(directoryUri)) {
            return false;
        }

        File folder = new File(directoryUri);
        return folder.mkdirs();
    }

    /**
     * Creates a new file
     *
     * @param fileUri Absolute path to the file
     * @return true, if created successfully. Else, false
     */
    @SneakyThrows
    public static boolean createFile(String fileUri) {
        if (checkFileExists(fileUri)) {
            return false;
        }

        File file = new File(fileUri);
        return file.createNewFile();
    }

    /**
     * Return File for given directory path
     *
     * @param directoryUri Absolute path of the directory
     * @return File, if file exists. Null, otherwise
     */
    public static File getFile(String directoryUri) {
        if (!checkFileExists(directoryUri)) {
            return null;
        }

        return new File(directoryUri);
    }

    public static boolean deleteFile(String fileUri) {
        File file = new File(fileUri);
        return file.delete();
    }

    /**
     * Check if a file/directory URI exists
     *
     * @param fileUri Absolute path of the file/directory
     * @return true, if file exists. false, otherwise.
     */
    public static boolean checkFileExists(String fileUri) {
        File file = new File(fileUri);
        return file.exists();
    }

    /**
     * Check if a URI is a directory
     *
     * @param fileUri Absolute path to file
     * @return True, if the given uri is a directory. False, otherwise
     */
    public static boolean isDirectory(String fileUri) {
        File file = new File(fileUri);
        return (file.exists() && file.isDirectory());
    }


    /**
     * Get the contents of a file
     *
     * @param fileUri Absolute path of file to be accessed
     * @return String, if file exists and is not empty
     */
    @SneakyThrows
    public static String getFileContents(String fileUri) {
        if (!FileHandler.checkFileExists(fileUri) || !FileHandler.isDirectory(fileUri)) {
            throw new Exception("File does not exist");
        }

        return new String(Files.readAllBytes(Paths.get(fileUri)));
    }

    /**
     * Write content to a file
     *
     * @param fileUri Absolute path of file
     * @param content Content to be written in file
     *                Note: Contents are overwritten, not appended
     */
    @SneakyThrows
    public static void writeFileContents(String fileUri, String content) {
        if (!FileHandler.checkFileExists(fileUri) || !FileHandler.isDirectory(fileUri)) {
            throw new Exception("File does not exist");
        }

        Files.write(Paths.get(fileUri), content.getBytes());
    }

}
