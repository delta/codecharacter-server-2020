package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.PasswordResetDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetDetailsRepository extends MongoRepository<PasswordResetDetails, Integer> {
    void deleteByUserId(Integer userId);

    PasswordResetDetails findByUserId(Integer userId);
}
