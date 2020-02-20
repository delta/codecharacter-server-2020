package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Constant;
import delta.codecharacter.server.repository.ConstantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ConstantService {
    private Logger LOG = Logger.getLogger(ConstantService.class.getName());

    @Autowired
    private ConstantRepository constantRepository;

    /**
     * Fetch the value present in Constant table
     *
     * @param key Key corresponding to the value
     * @return Value for the given key
     */
    public String getConstantValueByKey(String key) {
        Constant constant = constantRepository.findByKey(key);
        if (constant == null) return null;
        return constant.getValue();
    }
}
