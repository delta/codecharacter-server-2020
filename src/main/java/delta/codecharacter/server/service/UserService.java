package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.request.PublicUserRequest;
import delta.codecharacter.server.controller.request.RegisterUserRequest;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class UserService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

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

    private User getUser(Integer id) {
        return userRepository.findFirstById(id);
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
}
