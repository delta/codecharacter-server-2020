package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.response.LeaderboardResponse;
import delta.codecharacter.server.service.LeaderboardService;
import delta.codecharacter.server.util.PageUtils;
import delta.codecharacter.server.util.enums.Division;
import delta.codecharacter.server.util.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/leaderboard")
public class LeaderboardController {

    private final Logger LOG = Logger.getLogger(LeaderboardController.class.getName());

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping("/{PageNo}/{PageSize}")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboardData(@PathVariable @NotEmpty Integer PageNo, @PathVariable @NotEmpty Integer PageSize) {
        PageUtils.validatePaginationParams(PageNo, PageSize);
        return new ResponseEntity<List<LeaderboardResponse>>(leaderboardService.getLeaderboardData(PageNo, PageSize), HttpStatus.OK);
    }

    @GetMapping("/{search}/{PageNo}/{PageSize}")
    public ResponseEntity<List<LeaderboardResponse>> searchLeaderboardByUsername(@PathVariable @NotEmpty String search, @PathVariable @NotEmpty Integer PageNo, @PathVariable @NotEmpty Integer PageSize) {
        PageUtils.validatePaginationParams(PageNo, PageSize);
        return new ResponseEntity<List<LeaderboardResponse>>(leaderboardService.searchLeaderboardByUsernamePaginated(search, PageNo, PageSize), HttpStatus.OK);
    }

    @GetMapping("/division/{division}/{PageNo}/{PageSize}")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboardDataByDivision(@PathVariable @NotEmpty Division division, @PathVariable @NotEmpty Integer PageNo, @PathVariable @NotEmpty Integer PageSize) {
        PageUtils.validatePaginationParams(PageNo, PageSize);
        return new ResponseEntity<List<LeaderboardResponse>>(leaderboardService.getLeaderboardDataByDivisionPaginated(division, PageNo, PageSize), HttpStatus.OK);
    }

    @GetMapping("/userType/{userType}/{PageNo}/{PageSize}")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboardDataByUserType(@PathVariable @NotEmpty UserType userType, @PathVariable @NotEmpty Integer PageNo, @PathVariable @NotEmpty Integer PageSize) {
        PageUtils.validatePaginationParams(PageNo, PageSize);
        return new ResponseEntity<List<LeaderboardResponse>>(leaderboardService.getLeaderboardDataByUserType(userType, PageNo, PageSize), HttpStatus.OK);
    }

}
