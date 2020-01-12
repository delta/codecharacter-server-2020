package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.CommitCodeRequest;
import delta.codecharacter.server.controller.request.SaveCodeRequest;
import delta.codecharacter.server.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.logging.Logger;

@RestController
@RequestMapping("/code")
public class CodeController {
    final Logger LOG = Logger.getLogger(CodeController.class.getName());

    @Autowired
    private CodeService codeService;

    @RequestMapping(value = "/save/userId/{userId}/", method = RequestMethod.POST)
    public ResponseEntity<String> save(@RequestBody @Valid SaveCodeRequest request, @PathVariable("userId") Integer userId) {
        int statusCode = codeService.saveCode(request, userId);
        return getResponseEntityByHttpStatusCode(statusCode);
    }

    @RequestMapping(value = "/commit/userId/{userId}/", method = RequestMethod.POST)
    public ResponseEntity<String> commit(@RequestBody @Valid CommitCodeRequest request, @PathVariable("userId") Integer userId) {
        int statusCode = codeService.commitCode(request, userId);
        return getResponseEntityByHttpStatusCode(statusCode);
    }

    private ResponseEntity<String> getResponseEntityByHttpStatusCode(int statusCode) {
        switch (statusCode) {
            case 401:
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            case 403:
                return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            case 200:
                return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
            case 404:
                return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
