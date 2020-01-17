package delta.codecharacter.server;

import delta.codecharacter.server.repository.CodeStatusRepository;
import delta.codecharacter.server.repository.CodeVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodecharacterServer2019ApplicationTests {

    @Autowired
    CodeStatusRepository codeStatusRepository;

    @Autowired
    CodeVersionRepository codeVersionRepository;

    @Test
    void contextLoads() {
    }

    @Test
    public void deleteAll() {

    }
}
