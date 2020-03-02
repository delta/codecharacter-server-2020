package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.request.UpdateMatchRequest;
import delta.codecharacter.server.controller.response.Match.MatchResponse;
import delta.codecharacter.server.service.MatchService;
import delta.codecharacter.server.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping(value = "/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Value("${compilebox.secret-key}")
    private String compileboxSecretKey;

    @GetMapping(value = "/top/{PageNo}/{PageSize}")
    public ResponseEntity<List<MatchResponse>> getTopMatches(@PathVariable @NotEmpty Integer PageNo, @PathVariable @NotEmpty Integer PageSize) {
        PageUtils.validatePaginationParams(PageNo, PageSize);
        return new ResponseEntity<>(matchService.getTopMatches(PageNo, PageSize), HttpStatus.OK);
    }

    @PatchMapping(value = "")
    public void updateMatch(@RequestBody @Valid UpdateMatchRequest updateMatchRequest) {
        if (!updateMatchRequest.getSecretKey().equals(compileboxSecretKey))
            return;
        matchService.updateMatch(updateMatchRequest);
    }

}
