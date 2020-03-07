package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.Codeversion.CommitResponse;
import delta.codecharacter.server.controller.request.LockCodeRequest;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.service.VersionControlService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RestController
@RequestMapping(value = "/code")
public class CodeVersionController {
    private final Logger LOG = Logger.getLogger(CodeVersionController.class.getName());

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    UserService userService;

    @Value("${compilebox.secret-key}")
    private String secretKey;

    @GetMapping(value = "/latest")
    @SneakyThrows
    public ResponseEntity<String> getLatestCode(Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        String code = versionControlService.getCode(user.getUserId());
        if (code == null) return new ResponseEntity<>("Code repository not created", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @PutMapping(value = "")
    @SneakyThrows
    public ResponseEntity<String> saveCode(@RequestBody @Valid String code, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        if (!versionControlService.setCode(user.getUserId(), code))
            return new ResponseEntity<>("Code repository not created", HttpStatus.FORBIDDEN);
        return new ResponseEntity<>("Saved Code", HttpStatus.OK);
    }

    @PostMapping(value = "/commit")
    @SneakyThrows
    public ResponseEntity<String> commit(@RequestBody @Valid String commitMessage, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        String commitHash = versionControlService.commitCode(user.getUserId(), commitMessage);
        if (commitHash == null) return new ResponseEntity<>("Code repository not created", HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(commitHash, HttpStatus.OK);
    }

    @GetMapping(value = "/commit/{commitHash}")
    @SneakyThrows
    public ResponseEntity<String> viewCommitByHash(@PathVariable String commitHash, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        String code = versionControlService.getCodeByCommitHash(user.getUserId(), commitHash);
        if (code == null) return new ResponseEntity<>("Code repository not created", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }

    @GetMapping(value = "/log")
    public ResponseEntity<List<CommitResponse>> getLog(Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        List<CommitResponse> log = versionControlService.getLog(user.getUserId());
        if (log == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(log, HttpStatus.OK);
    }

    @GetMapping(value = "/last-saved-at")
    @SneakyThrows
    public ResponseEntity<String> getLatestSavedTime(Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        String lastCommittedTime = versionControlService.getLastSavedTime(user.getUserId());
        if (lastCommittedTime == null) return new ResponseEntity<>("Code repository not created", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(lastCommittedTime, HttpStatus.OK);
    }

    @PostMapping(value = "/fork/{commitHash}")
    @SneakyThrows
    public ResponseEntity<String> forkCommitByHash(@PathVariable String commitHash, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        if (!versionControlService.forkCommitByHash(user.getUserId(), commitHash))
            return new ResponseEntity<>("Code repository not created", HttpStatus.FORBIDDEN);
        return new ResponseEntity<>("Forked successfully", HttpStatus.OK);
    }

    @SneakyThrows
    @MessageMapping("/submit")
    public void submitCode(Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);

        versionControlService.submitCode(user.getUserId());
    }

    @PostMapping(value = "/lock")
    public void lockCode(@RequestBody LockCodeRequest lockCodeRequest) {
        if (!lockCodeRequest.getSecretKey().equals(secretKey))
            return;
        versionControlService.lockCode(lockCodeRequest);
    }
}
