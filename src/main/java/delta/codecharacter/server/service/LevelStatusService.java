package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Leaderboard;
import delta.codecharacter.server.model.LevelStatus;
import delta.codecharacter.server.repository.LevelStatusRepository;
import delta.codecharacter.server.util.enums.Division;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class LevelStatusService {

    @Autowired
    private LevelStatusRepository levelStatusRepository;

    /**
     * Initialize leveStatus for new user
     *
     * @param userId - User id of the new user
     */
    @Transactional
    public void initializeLevelStatus(@NotNull Integer userId) {
        List<Integer> initialStars = Arrays.asList(0,0,0,0);
        LevelStatus levelStatus = LevelStatus.builder()
                .userId(userId)
                .stars(initialStars)
                .build();

        levelStatusRepository.save(levelStatus);
    }


}
