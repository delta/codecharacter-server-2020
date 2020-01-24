package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.response.UserStatsResponse;
import delta.codecharacter.server.controller.api.UserStatsController;
import delta.codecharacter.server.model.UserStats;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class UserStatsService {

    private final Logger LOG = Logger.getLogger(UserStatsController.class.getName());

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private UserRepository userRepository;

    public UserStatsResponse getUserStats(@NotEmpty Integer userId) {
        UserStats userStat = userStatsRepository.findByUserId(userId);
            return UserStatsResponse.builder()
                    .userId(userStat.getUserId())
                    .totalMatches(userStat.getTotalMatches())
                    .totalWins(userStat.getTotalWins())
                    .totalLoses(userStat.getTotalLoses())
                    .lastMatchAt(userStat.getLastMatchAt())
                    .build();
    }

    @Transactional
    public void initializeUserStats(@NotEmpty Integer userId) {
        Date zeroDate=new Date();
        zeroDate.setTime(0);
        UserStats initialStats = UserStats.builder()
                .userId(userId)
                .totalMatches(0)
                .totalWins(0)
                .totalLoses(0)
                .lastMatchAt(zeroDate)
                .build();
        userStatsRepository.save(initialStats);
    }
}
