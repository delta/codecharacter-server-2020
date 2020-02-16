package delta.codecharacter.server;

import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.service.VersionControlService;
import delta.codecharacter.server.util.enums.AuthMethod;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class VersionControlTest {
    private final Logger LOG = Logger.getLogger(VersionControlTest.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private VersionControlService versionControlService;

    private User user;

    @BeforeEach
    public void init() {
        Integer userId = getMaxUserId() + 1;
        user = User.builder()
                .userId(userId)
                .username("<username>")
                .email("<email@example.com>")
                .fullName("<Full Name>")
                .isActivated(true)
                .authMethod(AuthMethod.MANUAL)
                .isAdmin(true)
                .build();

        versionControlService.createCodeRepository(user.getUserId(), user.getUsername());
    }

    @Test
    public void checkSetCode() {
        String code = "<code>";
        versionControlService.setCode(user.getUsername(), code);
        assertEquals(versionControlService.getCode(user.getUsername()), code);
    }

    @Test
    public void checkCommit() {
        String commitHash = versionControlService.commit(user.getUsername());
        assertNotNull(commitHash);
    }

    @Test
    public void checkLog() {
        Iterable<RevCommit> log = versionControlService.log(user.getUsername());
        assertNotNull(log);
        for (var revCommit : log) {
            assertNotNull(revCommit);
        }
    }

    private Integer getMaxUserId() {
        User user = userRepository.findFirstByOrderByUserIdDesc();
        System.out.println(user);
        if (user == null) {
            return 1;
        }
        return user.getUserId();
    }
}
