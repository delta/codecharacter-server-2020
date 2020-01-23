package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.UserActivation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivationRepository extends MongoRepository<UserActivation, Integer> {
    void deleteByUserId(Integer userId);

    UserActivation findByActivationToken(String activationToken);
}
