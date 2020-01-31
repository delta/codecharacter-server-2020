package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.UserRatingsResponse;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserRating;
import delta.codecharacter.server.repository.UserRatingRepository;
import delta.codecharacter.server.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
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

    /**
     * Get all the ratings of all a user
     *
     * @param username username of user
     * @return ratings of a User
     */
    @SneakyThrows
    public List<UserRatingsResponse> getUserRatings(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new Exception("Invalid username");

        Integer userId = user.getUserId();
        List<UserRating> ratings = userRatingRepository.findByUserId(userId);
        List<UserRatingsResponse> userRatings = new ArrayList<>();
        for (var rating : ratings) {
            userRatings.add(UserRatingsResponse.builder()
                    .rating(rating.getRating())
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
    @SneakyThrows
    @Transactional
    public void initializeUserRating(@NotEmpty Integer userId) {
        if (userRatingRepository.findByUserId(userId).size() > 0)
            throw new Exception("User already has ratings in the userRating collection");

        LocalDateTime currentDate = LocalDateTime.now();
        UserRating initialUserRating = UserRating.builder()
                .userId(userId)
                .rating(1200)
                .validFrom(currentDate)
                .build();
        userRatingRepository.save(initialUserRating);
    }

}
