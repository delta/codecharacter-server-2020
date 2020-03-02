package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.UserRatingsResponse;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserRating;
import delta.codecharacter.server.repository.UserRatingRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.Glicko.GlickoRating;
import delta.codecharacter.server.util.Glicko.RatingCalculator;
import delta.codecharacter.server.util.enums.Verdict;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class UserRatingService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserRatingRepository userRatingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private LeaderboardService leaderboardService;

    private RatingCalculator ratingCalculator;

    UserRatingService() {
        ratingCalculator = new RatingCalculator();
    }

    /**
     * Get all the ratings of a user
     *
     * @param username Username of user
     * @return ratings of a User
     */
    @SneakyThrows
    public List<UserRatingsResponse> getUserRatings(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new Exception("Invalid username");

        List<UserRating> ratings = userRatingRepository.findByUserId(user.getUserId());
        List<UserRatingsResponse> userRatings = new ArrayList<>();
        for (var rating : ratings) {
            userRatings.add(UserRatingsResponse.builder()
                    .rating(rating.getRating())
                    .ratingDeviation(rating.getRatingDeviation())
                    .validFrom(rating.getValidFrom())
                    .build());
        }
        return userRatings;
    }

    /**
     * Set initial details of new user in UserRating table
     *
     * @param userId userId of the user
     */
    @Transactional
    public void initializeUserRating(@NotEmpty Integer userId) {
        if (userRatingRepository.findByUserId(userId).size() > 0)
            return;

        UserRating initialUserRating = UserRating.builder()
                .userId(userId)
                .rating(1500d)
                .ratingDeviation(350d)
                .build();
        userRatingRepository.save(initialUserRating);
    }

    /**
     * Calculate updated ratings for a match
     *
     * @param userId1 UserId of player 1 (Match initiator)
     * @param userId2 UserId of player 2
     * @param verdict Verdict of match
     */
    public void calculateMatchRatings(Integer userId1, Integer userId2, Verdict verdict) {
        UserRating rating1 = userRatingRepository.findFirstByUserIdOrderByValidFromDesc(userId1);
        UserRating rating2 = userRatingRepository.findFirstByUserIdOrderByValidFromDesc(userId2);

        // Used to revert back the userRatingDeviation of player 2
        final Double user2RatingDeviation = rating2.getRatingDeviation();

        // Calculate weighted rating deviations for both players
        Double weightedRatingDeviation1 = ratingCalculator.calculateWeightedRatingDeviation(
                rating1.getRating(), matchService.getRecentMatchTime(userId1));
        Double weightedRatingDeviation2 = ratingCalculator.calculateWeightedRatingDeviation(
                rating2.getRating(), matchService.getRecentMatchTime(userId2));

        rating1.setRatingDeviation(weightedRatingDeviation1);
        rating2.setRatingDeviation(weightedRatingDeviation2);

        GlickoRating glickoRating1 = GlickoRating.builder()
                .rating(rating1.getRating())
                .ratingDeviation(rating1.getRatingDeviation())
                .build();

        GlickoRating glickoRating2 = GlickoRating.builder()
                .rating(rating2.getRating())
                .ratingDeviation(rating2.getRatingDeviation())
                .build();

        // Calculate player 1 new rating
        List<GlickoRating> opponentRatings1 = new ArrayList<>();
        opponentRatings1.add(glickoRating2);

        List<Double> matchScores1 = new ArrayList<>();
        matchScores1.add(getVerdictScore(verdict, false));

        Double newRating1 = ratingCalculator.calculateNewRating(glickoRating1, opponentRatings1, matchScores1);

        // Calculate player 2 new rating
        List<GlickoRating> opponentRatings2 = new ArrayList<>();
        opponentRatings1.add(glickoRating1);

        List<Double> matchScores2 = new ArrayList<>();
        matchScores1.add(getVerdictScore(verdict, true));

        Double newRating2 = ratingCalculator.calculateNewRating(glickoRating2, opponentRatings2, matchScores2);

        // Calculate new rating deviation of player 1
        Double newRatingDeviation1 = ratingCalculator.calculateNewRatingDeviation(glickoRating1, opponentRatings1);

        updateUserRating(userId1, newRating1, newRatingDeviation1);
        // Player 2 deviation doesn't change since he did not initiate match
        updateUserRating(userId2, newRating2, user2RatingDeviation);
    }

    private Double getVerdictScore(Verdict verdict, boolean isOpponent) {
        double score;

        switch (verdict) {
            case PLAYER_1:
                score = 1d;
                break;
            case PLAYER_2:
                score = 0d;
                break;
            default:
                score = 0.5d;
        }

        if (isOpponent) return (1d - score);

        return score;
    }

    /**
     * Add a new user rating entry of player in UserRating table
     *
     * @param userId          User Id
     * @param rating          Player rating
     * @param ratingDeviation Rating deviation of player
     */
    public void updateUserRating(Integer userId, Double rating, Double ratingDeviation) {

        UserRating userRating = UserRating.builder()
                .userId(userId)
                .rating(rating)
                .ratingDeviation(ratingDeviation)
                .build();

        userRatingRepository.save(userRating);

        leaderboardService.updateLeaderboardData(userId, rating);
    }
}
