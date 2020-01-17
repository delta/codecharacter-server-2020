package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.CommitCodeRequest;
import delta.codecharacter.server.controller.request.SaveCodeRequest;
import delta.codecharacter.server.controller.request.UserIdRequest;
import delta.codecharacter.server.controller.response.*;
import delta.codecharacter.server.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/code")
public class CodeController {
    final Logger LOG = Logger.getLogger(CodeController.class.getName());

    @Autowired
    private CodeService codeService;

    @RequestMapping(value = "/save/", method = RequestMethod.POST)
    public ResponseSuccess save(@RequestBody @Valid SaveCodeRequest request) throws ResponseException {
        Integer userId = request.getUserId();
        return codeService.saveCode(request, userId);
    }

    @RequestMapping(value = "/commit/", method = RequestMethod.POST)
    public PrivateCommitResponse commit(@RequestBody @Valid CommitCodeRequest request) throws ResponseException {
        Integer userId = request.getUserId();
        return codeService.commitCode(request, userId);
    }

    @GetMapping(value = "/latest")
    public PrivateCodeResponse getLastSavedCode(@RequestBody @Valid UserIdRequest userIdRequest) throws ResponseException {
        Integer userId = userIdRequest.getUserId();
        return codeService.getLastSavedCode(userId);
    }

    @GetMapping(value = "/view/{commitHash}")
    public PrivateCodeResponse viewCommit(@RequestBody @Valid UserIdRequest userIdRequest,
                                          @PathVariable("commitHash") String commitHash) throws ResponseException {
        Integer userId = userIdRequest.getUserId();
        return codeService.viewCommit(commitHash, userId);
    }

    @GetMapping(value = "/log")
    public PaginatedResponse<List<PrivateCommitResponse>> getCommitLog(@RequestBody @Valid UserIdRequest userIdRequest,
                                                                       @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                                                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                                       HttpServletRequest request) throws ResponseException {
        Integer userId = userIdRequest.getUserId();
        String requestUrl = request.getRequestURL().toString();
        return codeService.getCommitLog(userId, pageNumber, size, requestUrl);
    }

}
