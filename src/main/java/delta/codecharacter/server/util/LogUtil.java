package delta.codecharacter.server.util;

import delta.codecharacter.server.controller.response.GameLogs;
import delta.codecharacter.server.util.enums.LogId;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class LogUtil {

    @Value("storage/logs")
    private static String logStoragePath;

    /**
     * Return the absolute path to the log files directory of given gameId
     *
     * @param gameId - GameId of the user whose directory is to be accessed
     * @return Path to logs directory
     */
    public static String getLogRepositoryUri(Integer gameId) {
        return System.getProperty("user.dir") + File.separator + logStoragePath + File.separator + gameId;
    }

    /**
     * Return the absolute path to the player log file of given gameId
     *
     * @param gameId GameId of user whose log is to be accessed
     * @param logId  LogId of the log needed
     * @return Path to required log file
     */
    private static String getLogFileUri(Integer gameId, LogId logId) {
        return getLogRepositoryUri(gameId) + File.separator + logId;
    }

    /**
     * Create a new log repository for given gameId
     *
     * @param gameId GameId of the user
     */
    @SneakyThrows
    public static void createLogRepository(Integer gameId) {
        String logRepositoryUri = getLogRepositoryUri(gameId);
        if (!FileHandler.checkFileExists(logRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(logRepositoryUri);
            if (!dirCreated) {
                throw new Exception("Cannot create directory");
            }
        }
    }

    /**
     * Get log of given gameId
     *
     * @param gameId GameId of user
     * @param logId  LogId of the log needed
     * @return Contents of the required log file
     */
    public static String getLog(Integer gameId, LogId logId) {
        String logFileUri = getLogFileUri(gameId, logId);
        try {
            return FileHandler.getFileContents(logFileUri);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get game log, player 1 log and player 2 log of given gameId
     *
     * @param gameId GameId of user
     * @return Contents of the required log file
     */
    public static GameLogs getLogs(Integer gameId) {
        String gameLogFileUri = getLogFileUri(gameId, LogId.GAME_LOG);
        String playerLogFileUri = getLogFileUri(gameId, LogId.PLAYER_LOG_1);
        String playerLogFileUri2 = getLogFileUri(gameId, LogId.PLAYER_LOG_2);
        try {
            return GameLogs.builder()
                    .gameLog(FileHandler.getFileContents(gameLogFileUri))
                    .player1Log(FileHandler.getFileContents(playerLogFileUri))
                    .player2Log(FileHandler.getFileContents(playerLogFileUri2))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set contents of log file of given gameId
     *
     * @param gameId   GameId of user
     * @param gameLogs Log details of the game
     */
    public static void setLogs(Integer gameId, GameLogs gameLogs) {
        String logFileUri = getLogFileUri(gameId, LogId.GAME_LOG);
        String playerLogFileUri1 = getLogFileUri(gameId, LogId.PLAYER_LOG_1);
        String playerLogFileUri2 = getLogFileUri(gameId, LogId.PLAYER_LOG_2);
        FileHandler.writeFileContents(logFileUri, gameLogs.getGameLog());
        FileHandler.writeFileContents(playerLogFileUri1, gameLogs.getPlayer1Log());
        FileHandler.writeFileContents(playerLogFileUri2, gameLogs.getPlayer2Log());
    }
}
