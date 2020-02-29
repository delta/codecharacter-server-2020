package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.response.Match.MatchResponse;
import delta.codecharacter.server.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping(value = "/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping(value = "/top")
    public ResponseEntity<List<MatchResponse>> getMatchesByUserId() {
        return new ResponseEntity<>(matchService.getAllTopMatches(), HttpStatus.OK);
    }
}
