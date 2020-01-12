package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.CommitCodeRequest;
import delta.codecharacter.server.controller.request.SaveCodeRequest;
import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.CodeStatusRepository;
import delta.codecharacter.server.repository.CodeVersionRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.utils.GitHandler;
import delta.codecharacter.server.utils.GitRepository;
import delta.codecharacter.server.utils.Status;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CodeService {
    private final Logger LOG = Logger.getLogger(CodeService.class.getName());

    @Autowired
    private CodeStatusRepository codeStatusRepository;

    @Autowired
    private CodeVersionRepository codeVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Integer saveCode(SaveCodeRequest request, Integer userId) {
        CodeStatus codeStatus = getCodeStatus(userId);
        if (!codeStatus.getStatus().equals(Status.IDLE)) {
            return 403;
        }
        User user = findUserById(userId);
        if (user == null) {
            return 401;
        }
        String username = user.getUsername();
        String dir = GitHandler.getUserDir(username);
        String path = GitHandler.getUserCodeFilePath(username);
        File folder = new File(dir);
        File file = new File(path);
        LOG.info("File path: " + file.getAbsolutePath());
        try {
            boolean createdFolder = folder.mkdirs();
            boolean createdFile = file.createNewFile();

            if (createdFolder) {
                LOG.info("Created folder");
            } else {
                LOG.info("Folder exists");
            }

            if (createdFile) {
                LOG.info("Created file");
            } else {
                LOG.info("Folder exists");
            }

            Git git = GitHandler.createRepo(folder);
            GitRepository gitRepository = GitHandler.createGitRepository(git);
            LOG.info("GitRepository: " + gitRepository);
            boolean wasWritten = GitHandler.writeFile(file, request.getCode());
            boolean added = gitRepository.add();
            if (!added) {
                return 500;
            }
            return 200;
        } catch (Exception e) {
            return 500;
        }
    }

    @Transactional
    public int commitCode(CommitCodeRequest request, Integer userId) {
        CodeStatus codeStatus = getCodeStatus(userId);
        if (!codeStatus.getStatus().equals(Status.IDLE)) {
            return 403;
        }
        User user = findUserById(userId);
        String username = user.getUsername();
        String dir = GitHandler.getUserDir(username);
        String path = GitHandler.getUserCodeFilePath(username);
        File folder = new File(dir);
        File file = new File(path);
        LOG.info("File path: " + file.getAbsolutePath());
        try {
            boolean createdFolder = folder.mkdirs();
            boolean createdFile = file.createNewFile();

            if (createdFolder) {
                LOG.info("Created folder");
            } else {
                LOG.info("Folder exists");
            }

            if (createdFile) {
                LOG.info("Created file");
            } else {
                LOG.info("Folder exists");
            }

            Git git = GitHandler.createRepo(folder);
            GitRepository gitRepository = GitHandler.createGitRepository(git);
            LOG.info("GitRepository: " + gitRepository);
            boolean added = gitRepository.add();
            String commitHash = gitRepository.commit(request.getCommitMessage());
            LOG.info("Commit hash: " + commitHash);
            if (commitHash == null) {
                return 500;
            }
            return 200;
        } catch (Exception e) {
            return 500;
        }

    }

    public LocalDateTime getLastSavedAt() {
        return null;
    }

    public String getLastSavedCode() {
        return null;
    }

    private CodeStatus getCodeStatus(Integer userId) {
        CodeStatus codeStatus = codeStatusRepository.findFirstByUserId(userId);
        if (codeStatus == null) {
            codeStatus = CodeStatus.builder()
                    .userId(userId)
                    .status(Status.IDLE)
                    .build();
        }
        return codeStatus;
    }

    private User findUserById(Integer userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return null;
        }
        return optionalUser.get();
    }
}
