package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    User findFirstByOrderByUserIdDesc();

    User findByEmail(String email);

    User findByUsername(String username);

    List<User> findByUsernameRegex(String username);

    User findByUserId(Integer userId);
}
