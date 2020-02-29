package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.Leaderboard.LeaderboardData;
import delta.codecharacter.server.controller.response.Leaderboard.PublicLeaderboardResponse;
import delta.codecharacter.server.controller.response.LeaderboardAggregateResponse;
import delta.codecharacter.server.controller.response.LeaderboardResponse;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.model.Leaderboard;
import delta.codecharacter.server.model.UserRating;
import delta.codecharacter.server.repository.LeaderboardRepository;
import delta.codecharacter.server.repository.UserRatingRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.MatchStats;
import delta.codecharacter.server.util.enums.Division;
import delta.codecharacter.server.util.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class LeaderboardService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRatingRepository userRatingRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private ConstantService constantService;

    /**
     * Initialize leaderboard data for new user
     *
     * @param userId - User id of the new user
     */
    @Transactional
    public void initializeLeaderboardData(@NotNull Integer userId) {
        var initialRating = Double.valueOf(constantService.getConstantValueByKey("INITIAL_RATING"));
        Leaderboard leaderboard = Leaderboard.builder()
                .userId(userId)
                .division(Division.DIV_2)
                .rating(initialRating)
                .build();

        leaderboardRepository.save(leaderboard);
    }

    /**
     * Get rank of a user
     *
     * @param rating rating of user
     * @return rank of user
     */
    Integer getRank(Integer rating) {
        return leaderboardRepository.countByRatingGreaterThan(rating) + 1;
    }

    /**
     * Get all users with rank of given pageSize
     *
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of users of given pageSize
     */
    public List<PublicLeaderboardResponse> getLeaderboardData(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                lookup("user", "user_id", "_id", "join"),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return getLeaderboardResponseFromLeaderboardData(groupResults.getMappedResults());
    }

    /**
     * Search for users with regex-matching username of given pageSize
     *
     * @param username   username of user
     * @param pageNumber page number
     * @param pageSize   page size
     * @return return users with regex-matching username of given pageSize
     */
    public List<PublicLeaderboardResponse> searchLeaderboardByUsernamePaginated(String username, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                lookup("user", "user_id", "_id", "join"),
                match(Criteria.where("join.username").regex(username)),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return getLeaderboardResponseFromLeaderboardData(groupResults.getMappedResults());
    }

    /**
     * Get details of users playing in given division of given pageSize
     *
     * @param division   desired division
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of users playing in the given division of given pageSize
     */
    public List<PublicLeaderboardResponse> getLeaderboardDataByDivisionPaginated(Division division, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                match(Criteria.where("division").is(division)),
                lookup("user", "user_id", "_id", "join"),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return getLeaderboardResponseFromLeaderboardData(groupResults.getMappedResults());
    }

    /**
     * Get details of all users playing in given division
     *
     * @param division desired division
     * @return list of all users playing in the given division
     */
    public List<LeaderboardData> getLeaderboardDataByDivision(Division division) {
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                match(Criteria.where("division").is(division)),
                lookup("user", "user_id", "_id", "join"),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending()))
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return groupResults.getMappedResults();
    }

    /**
     * Get details of users of given userType
     *
     * @param userType   desired userType
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of users of given userType
     */
    public List<PublicLeaderboardResponse> getLeaderboardDataByUserTypePaginated(UserType userType, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                lookup("user", "user_id", "_id", "join"),
                match(Criteria.where("join.user_type").is(userType)),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return getLeaderboardResponseFromLeaderboardData(groupResults.getMappedResults());
    }

    /**
     * Get leaderboard data of users with given userType and division
     *
     * @param userType   userType to be filtered
     * @param division   division to be filtered
     * @param pageNumber page number
     * @param pageSize   page size
     * @return leaderboard data of the users of given userType and division
     */
    public List<PublicLeaderboardResponse> getLeaderboardDataByUserTypeAndDivisionPaginated(UserType userType, Division division, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("codeStatus", "user_id", "user_id", "userCodeStatus"),
                match(Criteria.where("userCodeStatus.is_locked").is(true)),
                lookup("user", "user_id", "_id", "join"),
                match(Criteria.where("join.user_type").is(userType).and("division").is(division)),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Leaderboard.class, LeaderboardData.class);

        return getLeaderboardResponseFromLeaderboardData(groupResults.getMappedResults());
    }

    /**
     * Build a list public leaderboard response from the given leaderboard data
     *
     * @param leaderboardData details of the leaderboard data
     * @return response to be sent
     */
    public List<PublicLeaderboardResponse> getLeaderboardResponseFromLeaderboardData(List<LeaderboardData> leaderboardData) {
        List<PublicLeaderboardResponse> leaderboardResponses = new ArrayList<>();
        for (var leaderboardItem : leaderboardData) {
            var userId = leaderboardItem.getUserId();

            // Get list of ratings of the user
            var userRatings = userRatingRepository.findByUserId(userId);

            // Get details of the user
            var user = userRepository.findByUserId(userId);

            // Get matches stats of user
            MatchStats matchStats = matchService.getMatchStatsByUsername(userRepository.findByUserId(userId).getUsername());

            PublicLeaderboardResponse leaderboardResponse = PublicLeaderboardResponse.builder()
                    .userId(userId)
                    .username(userRepository.findByUserId(userId).getUsername())
                    .division(leaderboardItem.getDivision())
                    .rating(userRatings)
                    .rank(getRank(userId))
                    .wins(matchStats.getWins())
                    .losses(matchStats.getLosses())
                    .ties(matchStats.getTies())
                    .country(user.getCountry())
                    .fullname(user.getFullName())
                    .userType(user.getUserType())
                    .avatarId(user.getAvatarId())
                    .build();
            leaderboardResponses.add(leaderboardResponse);
        }
        return leaderboardResponses;
    }

    /**
     * Update the leaderboard data of the user
     *
     * @param userId UserId of the user
     * @param rating new Rating of the user
     */
    public void updateLeaderboardData(Integer userId, Double rating) {
        Double div1Threshold = Double.valueOf(constantService.getConstantValueByKey("DIV_1_THRESHOLD"));
        var leaderboard = leaderboardRepository.findFirstByUserId(userId);

        leaderboard.setRating(rating);
        leaderboard.setDivision(rating > div1Threshold ? Division.DIV_1 : Division.DIV_2);

        leaderboardRepository.save(leaderboard);
    }
}
