package delta.codecharacter.server.utils;

import lombok.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class GitHandler {
    private String path;

    @Builder.Default
    private LocalDateTime lastSavedAt = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));

    public static Git createRepo(File dir) {
        try {
            Git git = Git.init().setDirectory(dir).call();
            return git;
        } catch (Exception e) {
            return null;
        }
    }

    public static GitRepository createGitRepository(Git git) {
        return new GitRepository(git);
    }

    public static boolean writeFile(File file, String text) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String readFile(File file) {
        try {
            return Files.readString(Paths.get(file.getPath()), StandardCharsets.US_ASCII);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserDir(String username) {
        return "/home/eswar/Desktop/jgit_trial/" + username;
    }

    public static String getUserCodeFilePath(String username) {
        return getUserDir(username) + "/code.cpp";
    }
}
