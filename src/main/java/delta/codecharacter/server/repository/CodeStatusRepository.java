package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.util.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CodeStatusRepository extends MongoRepository<CodeStatus, Integer> {
    CodeStatus findByUserId(Integer userId);

    CodeStatus findFirstByOrderByIdDesc();

    CodeStatus findAllByStatus(Status status);
}
