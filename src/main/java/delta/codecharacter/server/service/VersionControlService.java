package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Codeversion.CommitResponse;
import delta.codecharacter.server.controller.request.LockCodeRequest;
import delta.codecharacter.server.controller.request.Simulation.CompileCodeRequest;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class VersionControlService {

    private static final Logger LOG = Logger.getLogger(VersionControlService.class.getName());
    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Value("${compilebox.secret-key}")
    private String secretKey;

    @Value("/socket/response/alert/")
    private String socketAlertMessageDest;

    @Value("${storage.playercode.dir}")
    private String codeStoragePath;

    @Value("${storage.playerLockedcode.dir}")
    private String lockedCodeStoragePath;

    @Value("${storage.playercode.default.user.id}")
    private String defaultCodeUserId;

    @Value("${storage.playercode.filename}")
    private String codeFileName;

    @Value("${storage.playerLockedCode.filename}")
    private String lockedCodeFileName;

    @Autowired
    private RabbitMqService rabbitMqService;

    @Autowired
    private CodeStatusService codeStatusService;

    @Autowired
    private CodeStatusRepository codeStatusRepository;

    @Autowired
    private SocketService socketService;

    /**
     * Commit the saved code
     *
     * @param userId UserId of the given user
     * @return Commit hash of the created commit
     */
    @SneakyThrows
    public String commitCode(Integer userId, String commitMessage) {
        if (!checkCodeRepositoryExists(userId)) return null;
        gitAdd(userId);
        String commitHash = commit(userId, commitMessage);

        CodeStatus codeStatus = codeStatusService.getCodeStatusByUserId(userId);
        codeStatus.setCurrentCommit(commitHash);
        codeStatus.setLastSavedAt(new Date());
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
        CodeStatus codeStatus = codeStatusService.getCodeStatusByUserId(userId);
        return codeStatus.getLastSavedAt().toString();
    }

    /**
     * Get log of all commits
     *
     * @param userId UserId of the given user
     * @return Returns git log
     */
    @SneakyThrows
    public List<CommitResponse> getLog(Integer userId) {
        if (!checkCodeRepositoryExists(userId)) return null;
        List<CommitResponse> commitResponses = new ArrayList<>();
        for (RevCommit revCommit : log(userId)) {
            CommitResponse commitResponse = CommitResponse.builder()
                    .commitName(revCommit.getFullMessage())
                    .commitHash(revCommit.getName())
                    .timestamp(revCommit.getAuthorIdent().getWhen()).build();
            commitResponses.add(commitResponse);
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
     * Return the absolute path to the locked codes directory of given userId
     *
     * @param userId UserId of whose directory is to be accessed
     * @return Path to codes directory
     */
    private String getLockedCodeRepositoryUri(Integer userId) {
        return System.getProperty("user.dir") + File.separator + lockedCodeStoragePath + File.separator + userId;
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
     * Return the absolute path to the player locked code file of given userId
     *
     * @param userId UserId of whose code is to be accessed
     * @return Path to player locked code file
     */
    private String getLockedCodeFileUri(Integer userId) {
        return getLockedCodeRepositoryUri(userId) + File.separator + lockedCodeFileName;
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
     * Check if locked code repository exists
     *
     * @param userId UserId of the user
     * @return True if code repository exists, False otherwise
     */
    public boolean checkLockedCodeRepositoryExists(Integer userId) {
        String lockedCodeRepositoryUri = getLockedCodeRepositoryUri(userId);
        return FileHandler.checkFileExists(lockedCodeRepositoryUri);
    }

    /**
     * Create a new code repository with git initialized for given userId
     *
     * @param userId UserId of the user
     */
    @SneakyThrows
    public void createCodeRepository(Integer userId) {
        String codeRepositoryUri = getCodeRepositoryUri(userId);
        String lockedCodeRepositoryUri = getLockedCodeRepositoryUri(userId);

        if (!FileHandler.checkFileExists(codeRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(codeRepositoryUri);
        }
        if (!FileHandler.checkFileExists(lockedCodeRepositoryUri)) {
            boolean dirCreated = FileHandler.createDirectory(lockedCodeRepositoryUri);
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
        String defaultCode = FileHandler.getFileContents(getCodeFileUri(Integer.valueOf(defaultCodeUserId)));
        FileHandler.createFile(getLockedCodeFileUri(userId));
        FileHandler.writeFileContents(getCodeFileUri(userId), defaultCode);

        gitAdd(userId);
        commit(userId, "Initial Commit");

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
     * Commit the user's code repository
     *
     * @param userId UserId of user
     */
    @SneakyThrows
    public String commit(Integer userId, String commitMessage) {
        if (!checkCodeRepositoryExists(userId)) return null;

        Git git = Git.open(FileHandler.getFile(getCodeRepositoryUri(userId)));

        git.add().addFilepattern(".").call();
        // git commit -m "Commit #{commitCount}"
        RevCommit commit = git.commit()
                .setAuthor("Codecharacter", "codecharacter@pragyan.org")
                .setMessage(commitMessage)
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
        if (!checkCodeRepositoryExists(userId)) createCodeRepository(userId);
        String codeFileUri = getCodeFileUri(userId);
        FileHandler.writeFileContents(codeFileUri, code);

        //update lastSavedAt in codeStatus
        CodeStatus codeStatus = codeStatusRepository.findByUserId(userId);
        codeStatus.setLastSavedAt(new Date());
        codeStatusRepository.save(codeStatus);

        return true;
    }

    /**
     * Get locked code of given userId
     *
     * @param userId UserId of user
     * @return Contents of file
     */
    public String getLockedCode(Integer userId) {
        if (!checkLockedCodeRepositoryExists(userId)) return null;
        String lockedCodeFileUri = getLockedCodeFileUri(userId);
        return FileHandler.getFileContents(lockedCodeFileUri);
    }

    /**
     * Set locked code of given userId
     *
     * @param userId UserId of user
     */
    @SneakyThrows
    public void submitCode(Integer userId) {
        if (!checkLockedCodeRepositoryExists(userId))
            throw new Exception("No repository found");

        //Since code changes the dlls become obsolete
        DllUtil.deleteDllFile(userId, DllId.DLL_1);
        DllUtil.deleteDllFile(userId, DllId.DLL_2);

        String code = getCode(userId);

        var compileCodeRequest = CompileCodeRequest.builder()
                .userId(userId)
                .secretKey(secretKey)
                .code(code)
                .build();

        rabbitMqService.sendMessageToQueue(gson.toJson(compileCodeRequest));


    }

    public void lockCode(LockCodeRequest lockCodeRequest) {
        Integer userId = lockCodeRequest.getUserId();
        Boolean success = lockCodeRequest.getSuccess();
        if (!success) {
            socketService.sendMessage(socketAlertMessageDest + userId, "Failed to Submit!");
        }
        String lockedCodeFileUri = getLockedCodeFileUri(userId);

        //set isLocked to true in codeStatus table
        CodeStatus codeStatus = codeStatusService.getCodeStatusByUserId(userId);
        codeStatus.setLocked(true);
        codeStatusRepository.save(codeStatus);
    }
}
