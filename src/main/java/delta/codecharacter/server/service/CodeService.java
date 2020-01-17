package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.CommitCodeRequest;
import delta.codecharacter.server.controller.request.SaveCodeRequest;
import delta.codecharacter.server.controller.response.*;
import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.model.CodeVersion;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.CodeStatusRepository;
import delta.codecharacter.server.repository.CodeVersionRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.GitHandler;
import delta.codecharacter.server.util.PaginationUtil;
import delta.codecharacter.server.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public ResponseSuccess saveCode(SaveCodeRequest request, Integer userId) throws ResponseException {
        LOG.info("CodeStatuses: " + codeStatusRepository.findAll());
        CodeStatus codeStatus = getCodeStatusByUserId(userId);
        if (!codeStatus.getStatus().equals(Status.IDLE)) {
            throw ResponseException.getInternalServerErrorException();
        }
        User user = findUserById(userId);
        if (user == null) {
            throw ResponseException.getUnauthorizedResponseException();
        }
        try {
            String code = request.getCode();
            GitHandler.save(user.getUsername(), code);
            GitHandler.add(user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            throw ResponseException.getInternalServerErrorException();
        }
        ResponseSuccess response = ResponseSuccess.builder()
                .message("Saved successfully")
                .status(200)
                .build();
        return response;
    }

    @Transactional
    public PrivateCommitResponse commitCode(CommitCodeRequest request, Integer userId) throws ResponseException {
        CodeStatus codeStatus = getCodeStatusByUserId(userId);
        if (!codeStatus.getStatus().equals(Status.IDLE)) {
            throw ResponseException.getUnauthorizedResponseException();
        }
        User user = findUserById(userId);
        if (user == null) {
            throw ResponseException.getUnauthorizedResponseException();
        }

        String commitMessage = request.getCommitMessage();
        String commitHash;

        try {
            commitHash = GitHandler.commit(user.getUsername(), commitMessage);
        } catch (Exception e) {
            throw ResponseException.getInternalServerErrorException();
        }

        Integer codeVersionId = getMaxCodeVersionId() + 1;
        LocalDateTime now = LocalDateTime.now();

        CodeVersion codeVersion = CodeVersion.builder()
                .id(codeVersionId)
                .commitHash(commitHash)
                .userId(userId)
                .build();

        PrivateCommitResponse response = PrivateCommitResponse.builder()
                .commitHash(commitHash)
                .commitMessage(commitMessage)
                .userId(userId)
                .committedAt(now)
                .build();

        codeStatusRepository.save(codeStatus);
        codeVersionRepository.save(codeVersion);

        return response;
    }

    public LocalDateTime getLastSavedAt() {
        return null;
    }

    public PrivateCodeResponse getLastSavedCode(Integer userId) throws ResponseException {
        User user = findUserById(userId);
        if (user == null) {
            throw ResponseException.getUnauthorizedResponseException();
        }
        try {
            String code = GitHandler.readFileFromUsername(user.getUsername());
            PrivateCodeResponse response = PrivateCodeResponse.builder()
                    .code(code)
                    .build();
            return response;
        } catch (Exception e) {
            throw ResponseException.getInternalServerErrorException();
        }
    }

    public PrivateCodeResponse viewCommit(@NotNull String commitHash, @NotNull @Positive Integer userId) throws ResponseException {
        User user = findUserById(userId);
        if (user == null) {
            throw ResponseException.getUnauthorizedResponseException();
        }
        try {
            String code = GitHandler.getCommitFromHash(user.getUsername(), commitHash);

            PrivateCodeResponse response = PrivateCodeResponse.builder()
                    .code(code)
                    .build();
            return response;
        } catch (Exception e) {
            throw ResponseException.getInternalServerErrorException();
        }
    }

    public PaginatedResponse<List<PrivateCommitResponse>> getCommitLog(@NotNull @Positive Integer userId,
                                                                       @NotNull @Positive Integer pageNumber,
                                                                       @NotNull @Positive Integer size,
                                                                       String requestUrl) throws ResponseException {
        handlePaginationBadRequests(pageNumber, size);
        User user = findUserById(userId);

        if (user == null) {
            throw ResponseException.getUnauthorizedResponseException();
        }

        List<PrivateCommitResponse> commitResponses = new ArrayList<>();
        List<PrivateCommitResponse> paginatedCommitResponses = new ArrayList<>();

        try {
            commitResponses = GitHandler.getCommitLog(user.getUsername());
            paginatedCommitResponses = PaginationUtil.getPaginatedList(commitResponses, pageNumber, size);
        } catch (Exception e) {
            throw ResponseException.getInternalServerErrorException();
        }

        for (var commitResponse : paginatedCommitResponses) {
            commitResponse.setUserId(userId);
        }

        Long totalCount = (long) commitResponses.size();
        Integer totalPages = PaginationUtil.getTotalPages(commitResponses, size);
        String previousUrl = getPrevPageUrl(requestUrl, pageNumber, size, totalPages);
        String nextUrl = getNextPageUrl(requestUrl, pageNumber, size, totalPages);

        PaginatedResponse<List<PrivateCommitResponse>> response = PaginatedResponse.<List<PrivateCommitResponse>>builder()
                .count(paginatedCommitResponses.size())
                .page(pageNumber)
                .totalCount(totalCount)
                .previous(previousUrl)
                .next(nextUrl)
                .results(paginatedCommitResponses)
                .build();

        return response;
    }

    private String getPrevPageUrl(String requestBaseUrl, Integer pageNumber, Integer size, Integer totalPages) {
        if (pageNumber <= 1) {
            return null;
        }
        return requestBaseUrl + "?page=" + (pageNumber - 1) + "&size=" + size;
    }

    private String getNextPageUrl(String requestBaseUrl, Integer pageNumber, Integer size, Integer totalPages) {
        if (pageNumber >= totalPages) {
            return null;
        }
        return requestBaseUrl + "?page=" + (pageNumber + 1) + "&size=" + size;
    }

    private void handlePaginationBadRequests(Integer pageNumber, Integer size) throws ResponseException {
        if (pageNumber < 1) {
            throw ResponseException.getBadRequestException("Page number should not be less than 1");
        }
        if (size < 1) {
            throw ResponseException.getBadRequestException("Size should not be less than 1");
        }
        if (size > 100) {
            throw ResponseException.getBadRequestException("Size should not be greater than 100");
        }
    }

    private Integer getMaxCodeVersionId() {
        CodeVersion codeVersion = codeVersionRepository.findFirstByOrderByIdDesc();
        if (codeVersion == null) {
            return 0;
        }
        return codeVersion.getId();
    }

    private Integer getMaxCodeStatusId() {
            CodeStatus lastCodeStatus = codeStatusRepository.findFirstByOrderByIdDesc();
            if (lastCodeStatus == null)  {
                return 0;
            }
            return lastCodeStatus.getId();
    }

    private CodeStatus getCodeStatusByUserId(Integer userId) {
        CodeStatus codeStatus = codeStatusRepository.findByUserId(userId);
        if (codeStatus == null) {
            Integer codeStatusId = getMaxCodeStatusId() + 1;
            codeStatus = CodeStatus.builder()
                    .id(codeStatusId)
                    .userId(userId)
                    .build();
            LOG.info("Creating code status: " + codeStatus);
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
