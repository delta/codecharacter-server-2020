package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.CodeVersion;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public interface CodeVersionRepository extends MongoRepository<CodeVersion, Integer> {
    CodeVersion findFirstByOrderByIdDesc();

    CodeVersion findFirstByCommitHash(@NotNull String commitHash);

    CodeVersion findAllByUserIdOrderByIdDesc(@NotNull @Positive Integer userId);
}