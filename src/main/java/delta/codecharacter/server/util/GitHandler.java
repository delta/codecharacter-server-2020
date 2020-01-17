package delta.codecharacter.server.util;

import delta.codecharacter.server.controller.response.PrivateCommitResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class GitHandler {
    private static Git createRepo(File dir) throws GitAPIException {
        return Git.init().setDirectory(dir).call();
    }

    private static GitRepository createGitRepository(Git git) {
        return new GitRepository(git);
    }

    private static GitRepository createGitRepository(String username) throws GitAPIException, IOException {
        String dir = getUserDir(username);
        String path = getUserCodeFilePath(username);
        File folder = new File(dir);
        File file = new File(path);

        folder.mkdirs();
        file.createNewFile();

        Git git = createRepo(folder);
        GitRepository gitRepository = createGitRepository(git);

        return gitRepository;
    }

    public static void save(String username, String code) throws IOException, GitAPIException {
        createGitRepository(username);
        File file = new File(getUserCodeFilePath(username));
        writeFile(file, code);
    }

    public static void add(String username) throws IOException, GitAPIException {
        GitRepository gitRepository = createGitRepository(username);
        gitRepository.add();
    }

    public static String commit(String username, String commitMessage) throws IOException, GitAPIException {
        GitRepository gitRepository = createGitRepository(username);
        return gitRepository.commit(commitMessage);
    }

    public static String getCommitFromHash(String username, String commitHash) throws IOException, GitAPIException {
        GitRepository gitRepository = createGitRepository(username);
        String path = getUserCodeFilePath(username);
        File file = new File(path);
        return gitRepository.getContent(commitHash, file);
    }

    public static List<PrivateCommitResponse> getCommitLog(String username) throws IOException, GitAPIException {
        GitRepository gitRepository = createGitRepository(username);
        return gitRepository.log();
    }

    public static void writeFile(File file, String text) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }

    public static String readFile(File file) throws IOException {
        return Files.readString(Paths.get(file.getPath()), StandardCharsets.US_ASCII);
    }

    public static String readFileFromUsername(String username) throws IOException {
        File file = new File(getUserCodeFilePath(username));
        return readFile(file);
    }

    public static String getUserDir(String username) {
        return "/home/eswar/Desktop/jgit_trial/" + username;
    }

    public static String getUserCodeFilePath(String username) {
        return getUserDir(username) + "/code.cpp";
    }
}
