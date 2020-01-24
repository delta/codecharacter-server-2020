package delta.codecharacter.server.service;

import delta.codecharacter.server.util.FileHandler;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class VersionControlService {

    @Value("${storage.playercode.dir}")
    private String codeStoragePath;

    @Value("${storage.playercode.filename}")
    private String codeFileName;

    /**
     * Return the absolute path to the codes directory of given username
     *
     * @param username - Username of whose directory is to be accessed
     * @return Path to codes directory
     */
    private String getCodeRepositoryUri(String username) {
        return System.getProperty("user.dir") + codeStoragePath + File.separator + username;
    }

    /**
     * Return the absolute path to the player code file of given username
     *
     * @param username - Username of whose code is to be accessed
     * @return Path to player code file
     */
    private String getCodeFileUri(String username) {
        return getCodeRepositoryUri(username) + File.separator + codeFileName;
    }

    /**
     * Create a new code repository with git initialized for given username
     *
     * @param username Username of the user
     */
    @SneakyThrows
    public void createCodeRepository(Integer userId, String username) {
        String codeRepositoryUri = getCodeRepositoryUri(username);

        if (!FileHandler.checkFileExists(codeRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(codeRepositoryUri);
            if (!dirCreated) {
                throw new Exception("Cannot create directory");
            }
        }

        // git init
        Git git = Git.init().setDirectory(FileHandler.getFile(codeRepositoryUri)).call();

        Repository repository = git.getRepository();
        if (repository == null) {
            git.close();
            throw new Exception("Repository cannot be created");
        }

        // Create code file, add and commit
        if (!FileHandler.createFile(getCodeFileUri(username))) {
            git.close();
            throw new Exception("Code file cannot be created");
        }

        add(username);
        commit(username);

        git.close();
    }

    /**
     * Add all files to stage
     *
     * @param username Username of user
     */
    @SneakyThrows
    private void add(String username) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(username)));
        // git add .
        git.add().addFilepattern(".").call();
        git.close();
    }

    /**
     * Get git log of user's code repository
     *
     * @param username Username of user
     * @return Iterable of commits
     */
    @SneakyThrows
    public Iterable<RevCommit> log(String username) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(username)));
        Repository repository = git.getRepository();
        ObjectId HEAD = repository.resolve("refs/heads/master");

        // git log on master
        Iterable<RevCommit> log = git.log().add(HEAD).call();
        git.close();

        return log;
    }

    /**
     * Get number of commits in user's code repository
     *
     * @param username Username of user
     * @return long Number of commits
     */
    @SneakyThrows
    private long getCommitCount(String username) {
        var log = log(username);
        long commitCount = log.spliterator().getExactSizeIfKnown();

        // Iterable is not sized
        if (commitCount == -1) {
            commitCount = 0;
        }

        return commitCount;
    }

    /**
     * Commit the user's code repository
     *
     * @param username Username of user
     */
    @SneakyThrows
    public void commit(String username) {
        var commitCount = getCommitCount(username);
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(username)));

        // git commit -m "Commit #{commitCount}"
        git.commit()
                .setAuthor("Codecharacter", "codecharacter@pragyan.org")
                .setMessage("Commit #" + commitCount)
                .call();
        git.close();
    }

    /**
     * Checkout to a given commit hash in a user's code repository
     *
     * @param username   Username of the user
     * @param commitHash Commit Hash to checkout to
     */
    @SneakyThrows
    public void checkout(String username, String commitHash) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(username)));

        // If already checked out, need to reset head to master
        git.checkout().setName("master").call();
        // git checkout {commitHash}
        git.checkout().setName(commitHash).call();
        git.close();
    }

    /**
     * Reset HEAD to master if HEAD is detached
     *
     * @param username Username of the user
     */
    @SneakyThrows
    public void resetHead(String username) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(username)));

        // git checkout master
        git.checkout().setName("master").call();
        git.close();
    }

    /**
     * Get code of given username
     *
     * @param username Username of user
     * @return Contents of file
     */
    public String getCode(String username) {
        String codeFileUri = getCodeFileUri(username);
        return FileHandler.getFileContents(codeFileUri);
    }

    /**
     * Set code of given username
     *
     * @param username Username of user
     * @param code     Code to be inside the code file
     */
    public void setCode(String username, String code) {
        String codeFileUri = getCodeFileUri(username);
        FileHandler.writeFileContents(codeFileUri, code);
    }
}
