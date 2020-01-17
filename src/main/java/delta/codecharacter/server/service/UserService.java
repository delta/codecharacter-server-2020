package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.PublicUserRequest;
import delta.codecharacter.server.controller.request.RegisterUserRequest;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.AuthMethod;
import delta.codecharacter.server.util.UserAuthUtil.CustomUserDetails;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private final Logger LOG = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void registerUser(@NotNull RegisterUserRequest user) {
        Integer userId = getMaxUserId() + 1;

        User newUser = User.builder()
                .id(userId)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        userRepository.save(newUser);
    }

    private Integer getMaxUserId() {
        User user = userRepository.findFirstByOrderByIdDesc();
        System.out.println(user);
        if (user == null) {
            return 1;
        }
        return user.getId();
    }

    public List<PublicUserRequest> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<PublicUserRequest> publicUsers = new ArrayList<>();

        for (var user : users) {
            publicUsers.add(PublicUserRequest.builder()
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .build());
        }

        return publicUsers;
    }

    public boolean isEmailPresent(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    public boolean isUsernamePresent(String userName) {
        User user = userRepository.findByUsername(userName);
        return user != null;
    }

    @SneakyThrows
    @Transactional
    public void registerOAuthUser(Map<String, String> userDetails) {
        Integer userId = getMaxUserId() + 1;

        String email = userDetails.get("email");
        String name = userDetails.get("name");
        String username = userDetails.get("login");

        if (username == null) username = email.split("@")[0];
        if (name == null) name = email.split("@")[0];

        User newUser = User.builder()
                .id(userId)
                .email(email)
                .fullName(name)
                .username(username)
                .authMethod(AuthMethod.SSO)
                .isActivated(true)
                .build();

        userRepository.save(newUser);
    }

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //TODO: Check for email in Pragyan DB

        User user = userRepository.findByEmail(email);

        if (user == null)
            throw new UsernameNotFoundException("Email Not Found");

        //Check AuthType
        if (user.getAuthMethod().equals(AuthMethod.MANUAL))
            return new CustomUserDetails(user);

        //AuthType is not PRAGYAN and MANUAL
        throw new Exception("Use Github/Google to Login");
    }
}
