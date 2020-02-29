package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Map;
import delta.codecharacter.server.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapService {

    @Autowired
    private MapRepository mapRepository;

    public List<Map> getAllMaps() {
        return mapRepository.findAll();
    }
}
