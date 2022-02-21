package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.response.LevelStatusResponse;
import delta.codecharacter.server.model.Leaderboard;
import delta.codecharacter.server.model.LevelStatus;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.LevelStatusRepository;
import delta.codecharacter.server.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    /**
     * Initialize leveStatus for new user
     *
     * @param userId - User id of the new user
     */
    @Transactional
    public void initializeLevelStatus(@NotNull Integer userId) {
        if (levelStatusRepository.findByUserId(userId) != null)
            return;
        List<Integer> initialStars = Arrays.asList(0);
        LevelStatus levelStatus = LevelStatus.builder()
                .userId(userId)
                .stars(initialStars)
                .build();

        levelStatusRepository.save(levelStatus);
    }

    /**
     * Return LevelStatus of a user
     *
     * @param userId - User id of the user
     * @return LevelStatusResponse - Level and stars of that level of the user
     */
    public List<LevelStatusResponse> getLevelStatus(Integer userId){
        List<Integer> levelStatus = levelStatusRepository.findByUserId(userId).getStars();
        Integer currentLevel = userRepository.findByUserId(userId).getCurrentLevel();
        List<LevelStatusResponse> levelStatuses = new ArrayList<>();

        for(int i=0 ; i< currentLevel;i++){
            levelStatuses.add(LevelStatusResponse.builder()
                    .level(i + 1)
                    .stars(levelStatus.get(i))
                    .build());
        }
        return levelStatuses;
    }

    /**
     * Update LevelStatus after the game
     *
     * @param userId - User id of the user
     * @param levelNumber - current level for which the game is played(initial level 1)
     * @param starsCount - stars got in the game
     */
    public void updateLevelStatus(Integer userId, Integer levelNumber, Integer starsCount){
        LevelStatus levelStatus = levelStatusRepository.findByUserId(userId);
        List<Integer> stars = levelStatus.getStars();
        if(stars.size() == levelNumber){
            stars.add(0);
            User user = userRepository.findByUserId(userId);
            user.setCurrentLevel(stars.size());
            userRepository.save(user);
        }
        if(stars.size() > levelNumber && stars.get(levelNumber-1) < starsCount){
            stars.set(levelNumber-1,starsCount);
        }
        levelStatusRepository.save(levelStatus);
    }   
}
