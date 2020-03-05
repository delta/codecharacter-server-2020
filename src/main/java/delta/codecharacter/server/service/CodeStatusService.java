package delta.codecharacter.server.service;

import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.repository.CodeStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeStatusService {

    @Autowired
    CodeStatusRepository codeStatusRepository;

    /**
     * Find CodeStatus by userId
     *
     * @param userId userId of the given user
     * @return CodeStatus details of the given user. Creates CodeStatus and returns it, if it doesn't already exist.
     */
    public CodeStatus getCodeStatusByUserId(Integer userId) {
        CodeStatus codeStatus = codeStatusRepository.findByUserId(userId);
        if (codeStatus == null) {
            codeStatus = CodeStatus.builder().
                    userId(userId).
                    build();
        }
        return codeStatus;
    }

    public void initializeCodeStatusData(Integer userId) {
        Integer id = getMaxId() + 1;
        var codeStatus = CodeStatus.builder()
                .userId(getMaxId())
                .userId(userId)
                .build();

        codeStatusRepository.save(codeStatus);
    }

    public Integer getMaxId() {
        var codeStatus = codeStatusRepository.findFirstByOrderByIdDesc();
        if (codeStatus == null) {
            return 0;
        }
        return codeStatus.getId();
    }
}
