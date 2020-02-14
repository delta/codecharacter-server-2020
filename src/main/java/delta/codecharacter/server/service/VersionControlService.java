package delta.codecharacter.server.service;

import delta.codecharacter.server.util.DllFile;
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
     * Return the absolute path to the codes directory of given userId
     *
     * @param userId - Username of whose directory is to be accessed
     * @return Path to codes directory
     */
    private String getCodeRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + codeStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player code file of given userId
     *
     * @param userId - Username of whose code is to be accessed
     * @return Path to player code file
     */
    private String getCodeFileUri(Integer userId) {
        return getCodeRepositoryUri(userId) + File.separator + codeFileName;
    }

    /**
     * Create a new code repository with git initialized for given userId
     *
     * @param userId Username of the user
     */
    @SneakyThrows
    public void createCodeRepository(Integer userId) {
        String codeRepositoryUri = getCodeRepositoryUri(userId);

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
        if (!FileHandler.createFile(getCodeFileUri(userId))) {
            git.close();
            throw new Exception("Code file cannot be created");
        }

        add(userId);
        commit(userId);

        git.close();
    }

    /**
     * Add all files to stage
     *
     * @param userId Username of user
     */
    @SneakyThrows
    private void add(Integer userId) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));
        // git add .
        git.add().addFilepattern(".").call();
        git.close();
    }

    /**
     * Get git log of user's code repository
     *
     * @param userId Username of user
     * @return Iterable of commits
     */
    @SneakyThrows
    public Iterable<RevCommit> log(Integer userId) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));
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
     * @param userId Username of user
     * @return long Number of commits
     */
    @SneakyThrows
    private long getCommitCount(Integer userId) {
        var log = log(userId);
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
     * @param userId Username of user
     */
    @SneakyThrows
    public void commit(Integer userId) {
        var commitCount = getCommitCount(userId);
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

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
     * @param userId     Username of the user
     * @param commitHash Commit Hash to checkout to
     */
    @SneakyThrows
    public void checkout(Integer userId, String commitHash) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        // If already checked out, need to reset head to master
        git.checkout().setName("master").call();
        // git checkout {commitHash}
        git.checkout().setName(commitHash).call();
        git.close();
    }

    /**
     * Reset HEAD to master if HEAD is detached
     *
     * @param userId Username of the user
     */
    @SneakyThrows
    public void resetHead(Integer userId) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        // git checkout master
        git.checkout().setName("master").call();
        git.close();
    }

    /**
     * Get code of given userId
     *
     * @param userId Username of user
     * @return Contents of file
     */
    public String getCode(Integer userId) {
        String codeFileUri = getCodeFileUri(userId);
        return FileHandler.getFileContents(codeFileUri);
    }

    /**
     * Set code of given userId
     *
     * @param userId UserId of user
     * @param code   Code to be inside the code file
     */
    public void setCode(Integer userId, String code) {

        //Since code changes the dlls become obsolete
        DllFile.deleteDllDirectory(userId);

        String codeFileUri = getCodeFileUri(userId);
        FileHandler.writeFileContents(codeFileUri, code);
    }
}
