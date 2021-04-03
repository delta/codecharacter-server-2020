package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.SubmitStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmitStatusRepository extends MongoRepository<SubmitStatus, Integer> {
    SubmitStatus findFirstByOrderByIdDesc();
    SubmitStatus findByUserId(Integer userId);
}
