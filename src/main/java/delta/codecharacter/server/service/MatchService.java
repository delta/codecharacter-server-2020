package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.UserStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.Verdict;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class MatchService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @SneakyThrows
    public UserStatsResponse getUserStats(@NotEmpty String username) {
        if (userRepository.findByUsername(username)==null)
            throw new Exception("Invalid username");
        Integer userId=userRepository.findByUsername(username).getUserId();

        List<Match> player1matches=matchRepository.findByPlayerId1OrderByIdDesc(userId);
        List<Match> player2matches=matchRepository.findByPlayerId2OrderByIdDesc(userId);
        Integer wins=0,losses=0,ties=0,totalMatches=0;

        Date lastMatchAt;
        Date player1LastMatch=player1matches.get(0).getCreatedAt();
        Date player2LastMatch=player2matches.get(0).getCreatedAt();
        lastMatchAt=(player1LastMatch.getTime()>player2LastMatch.getTime()) ? player1LastMatch : player2LastMatch;

        for (var player1match : player1matches) {
            if (player1match.getVerdict()== Verdict.PLAYER_1)
                wins++;
            else if (player1match.getVerdict()==Verdict.PLAYER_2)
                losses++;
            else
                ties++;
            totalMatches++;
        }
        for (var player2match : player2matches) {
            if (player2match.getVerdict()== Verdict.PLAYER_2)
                wins++;
            else if (player2match.getVerdict()==Verdict.PLAYER_1)
                losses++;
            else
                ties++;
            totalMatches++;
        }
        return UserStatsResponse.builder()
                .userId(userId)
                .totalMatches(totalMatches)
                .totalWins(wins)
                .totalLoses(losses)
                .totalTies(ties)
                .lastMatchAt(lastMatchAt)
                .build();
    }

    @SneakyThrows
    public String getWaitTime(@NotEmpty String username){
        String response;
        if (userRepository.findByUsername(username)==null)
            throw new Exception("Invalid username");

        Integer userId=userRepository.findByUsername(username).getUserId();
        Float minWaitTime=Float.parseFloat(constantRepository.findByKey("MATCH_WAIT_TIME").getValue());
        Date lastMatchTime=getUserStats(username).getLastMatchAt();
        Date currentTime=new Date();

        Long timepassed=(currentTime.getTime()-lastMatchTime.getTime())/60000;
        if (timepassed>minWaitTime)
            return "Success!";
        else
            return (minWaitTime-timepassed)+" minutes left!";
    }

}
