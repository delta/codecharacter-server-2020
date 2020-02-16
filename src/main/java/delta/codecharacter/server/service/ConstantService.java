package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Constant;
import delta.codecharacter.server.repository.ConstantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ConstantService {
    private final Logger LOG = Logger.getLogger(ConstantService.class.getName());

    @Autowired
    private ConstantRepository constantRepository;

    public boolean isKeyMatches(String key, String value) {
        Constant constant = constantRepository.findByKey(key);
        if (constant == null) {
            return false;
        }
        return constant.getValue().equals(value);
    }
}
