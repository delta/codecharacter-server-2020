package delta.codecharacter.server.service;

import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.repository.CodeStatusRepository;
import delta.codecharacter.server.util.DllUtil;
import delta.codecharacter.server.util.FileHandler;
import delta.codecharacter.server.util.enums.DllId;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class VersionControlService {

    private static final Logger LOG = Logger.getLogger(VersionControlService.class.getName());

    @Value("${storage.playercode.dir}")
    private String codeStoragePath;

    @Value("${storage.playercode.filename}")
    private String codeFileName;

    @Autowired
    CodeStatusRepository codeStatusRepository;

    /**
     * Commit the saved code
     *
     * @param userId UserId of the given user
     * @return Commit hash of the created commit
     */
    @SneakyThrows
    public String commitCode(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;
        gitAdd(userId);
        String commitHash = commit(userId);

        CodeStatus codeStatus = findCodeStatusByUserId(userId);
        codeStatus.setCurrentCommit(commitHash);
        codeStatus.setLastSavedAt(LocalDateTime.now());
        codeStatusRepository.save(codeStatus);

        return commitHash;
    }

    /**
     * Get the last saved time for the given user
     *
     * @param userId UserId of the given user
     * @return Last saved time for the given user
     */
    @SneakyThrows
    public String getLastSavedTime(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;
        CodeStatus codeStatus = findCodeStatusByUserId(userId);
        return codeStatus.getLastSavedAt().toString();
    }

    /**
     * View the contents of file at a particular commit
     *
     * @param userId     UserId of the given user
     * @param commitHash Commit hash of commit
     * @return Details of the code
     */
    @SneakyThrows
    public String getCodeByCommitHash(Integer userId, String commitHash) {
        if (!checkCodeRepositoryExists(userId)) return null;

        checkout(userId, commitHash);
        String code = getCode(userId);
        resetHead(userId);

        return code;
    }

    /**
     * Get log of all commits
     *
     * @param userId UserId of the given user
     * @return Returns git log
     */
    @SneakyThrows
    public List<String> getLog(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;
        List<String> commitResponses = new ArrayList<>();
        for (RevCommit revCommit : log(userId)) {
            commitResponses.add(revCommit.getName());
        }
        return commitResponses;
    }

    /**
     * Fork one's own commit
     *
     * @param userId     UserId of the user
     * @param commitHash Commit hash of the required commit
     * @return True if the fork was successful, False otherwise
     */
    @SneakyThrows
    public boolean forkCommitByHash(Integer userId, String commitHash) {
        if (!checkCodeRepositoryExists(userId)) return false;
        checkout(userId, commitHash);
        String code = getCode(userId);
        resetHead(userId);
        setCode(userId, code);
        gitAdd(userId);
        return true;
    }

    /**
     * Return the absolute path to the codes directory of given userId
     *
     * @param userId UserId of whose directory is to be accessed
     * @return Path to codes directory
     */
    private String getCodeRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + File.separator + codeStoragePath + File.separator + userId;
    }

    /**
     * Return the absolute path to the player code file of given userId
     *
     * @param userId UserId of whose code is to be accessed
     * @return Path to player code file
     */
    private String getCodeFileUri(Integer userId) {
        return getCodeRepositoryUri(userId) + File.separator + codeFileName;
    }

    /**
     * Check if code repository exists
     *
     * @param userId UserId of the user
     * @return True if code repository exists, False otherwise
     */
    public boolean checkCodeRepositoryExists(Integer userId) {
        String codeRepositoryUri = getCodeRepositoryUri(userId);
        return FileHandler.checkFileExists(codeRepositoryUri);
    }

    /**
     * Create a new code repository with git initialized for given userId
     *
     * @param userId UserId of the user
     */
    @SneakyThrows
    public void createCodeRepository(Integer userId) {
        String codeRepositoryUri = getCodeRepositoryUri(userId);

        if (!FileHandler.checkFileExists(codeRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(codeRepositoryUri);
        }

        // git init
        Git git = Git.init().setDirectory(FileHandler.getFile(codeRepositoryUri)).call();

        Repository repository = git.getRepository();
        if (repository == null) {
            git.close();
            throw new Exception("Repository cannot be created");
        }

        // Create code file, add and commit
        FileHandler.createFile(getCodeFileUri(userId));

        gitAdd(userId);

        git.close();
    }

    /**
     * Add all files to stage
     *
     * @param userId UserId of user
     */
    @SneakyThrows
    private void gitAdd(Integer userId) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));
        // git add .
        git.add().addFilepattern(".").call();
        git.close();
    }

    /**
     * Get git log of user's code repository
     *
     * @param userId UserId of user
     * @return Iterable of commits
     */
    @SneakyThrows
    private Iterable<RevCommit> log(Integer userId) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        Repository repository = git.getRepository();
        ObjectId HEAD = repository.resolve("refs/heads/master");

        if (HEAD == null) return new ArrayList<>();

        // git log on master
        Iterable<RevCommit> log = git.log().add(HEAD).call();
        git.close();

        return log;
    }

    /**
     * Get number of commits in user's code repository
     *
     * @param userId UserId of user
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
     * @param userId UserId of user
     */
    @SneakyThrows
    public String commit(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;

        var commitCount = getCommitCount(userId);
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        git.add().addFilepattern(".").call();
        // git commit -m "Commit #{commitCount}"
        RevCommit commit = git.commit()
                .setAuthor("Codecharacter", "codecharacter@pragyan.org")
                .setMessage("Commit #" + commitCount)
                .call();
        git.close();
        return commit.getName();
    }

    /**
     * Checkout to a given commit hash in a user's code repository
     *
     * @param userId     UserId of the user
     * @param commitHash Commit Hash to checkout to
     */
    @SneakyThrows
    public void checkout(Integer userId, String commitHash) {
        if (!checkCodeRepositoryExists(userId)) return;

        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        // If already checked out, need to reset head to master
        git.checkout().setName("master").call();
        // git checkout {commitHash}
        git.checkout().setName(commitHash).call();
        git.close();
    }

    /**
     * Get code present in the commit of given commit-hash
     *
     * @param userId     UserId of the user
     * @param commitHash Commit Hash to checkout to
     */
    @SneakyThrows
    public String getCodeByCommitHash(Integer userId, String commitHash) {
        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));
        Repository repository = git.getRepository();

        String code;

        // A RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(repository.resolve(commitHash));
            // Using commit's tree find the path
            RevTree tree = commit.getTree();

            // Try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(codeFileName));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file " + codeFileName);
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                code = new String(loader.getBytes());
            }

            revWalk.dispose();
        }
        git.close();
        return code;
    }

    /**
     * Reset HEAD to master if HEAD is detached
     *
     * @param userId UserId of the user
     */
    @SneakyThrows
    public void resetHead(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return;

        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        // git checkout master
        git.checkout().setName("master").call();
        git.close();
    }

    /**
     * Get code of given userId
     *
     * @param userId UserId of user
     * @return Contents of file
     */
    public String getCode(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;
        String codeFileUri = getCodeFileUri(userId);
        return FileHandler.getFileContents(codeFileUri);
    }

    /**
     * Set code of given userId
     *
     * @param userId UserId of user
     * @param code   Code to be inside the code file
     */
    public boolean setCode(Integer userId, String code) {
        //Since code changes the dlls become obsolete
        DllUtil.deleteDllFile(userId, DllId.DLL_1);
        DllUtil.deleteDllFile(userId, DllId.DLL_2);
        
        if (!checkCodeRepositoryExists(userId)) createCodeRepository(userId);
        String codeFileUri = getCodeFileUri(userId);
        FileHandler.writeFileContents(codeFileUri, code);
        return true;
    }

    private CodeStatus findCodeStatusByUserId(Integer userId) {
        CodeStatus codeStatus = codeStatusRepository.findByUserId(userId);
        if (codeStatus == null) {
            codeStatus = CodeStatus.builder().
                    userId(userId).
                    build();
        }
        return codeStatus;
    }
}