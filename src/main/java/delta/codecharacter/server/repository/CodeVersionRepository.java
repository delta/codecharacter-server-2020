package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeVersionRepository extends MongoRepository<CodeVersion, Integer> {
    CodeVersion findFirstByOrderByIdDesc();

    List<CodeVersion> findAllByUserId(Integer userId);
}