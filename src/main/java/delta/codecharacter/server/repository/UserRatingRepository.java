package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.UserRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRatingRepository extends MongoRepository<UserRating, Integer> {
    List<UserRating> findByUserId(Integer userId);

    UserRating findFirstByUserIdOrderByValidFromDesc(Integer userId);
}
