package delta.codecharacter.server.service;

import delta.codecharacter.server.util.enums.Page;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getSuccessfulLoginString() {
        return String.valueOf(Page.successfulLogin);
    }
}
