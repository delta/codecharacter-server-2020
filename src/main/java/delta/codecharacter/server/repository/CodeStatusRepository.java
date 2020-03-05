package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.util.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeStatusRepository extends MongoRepository<CodeStatus, Integer> {
    CodeStatus findByUserId(Integer userId);

    List<CodeStatus> findAllByStatus(Status status);

    List<CodeStatus> findAllByOrderByLastSavedAtDesc();

    CodeStatus findFirstByOrderByIdDesc();
}
