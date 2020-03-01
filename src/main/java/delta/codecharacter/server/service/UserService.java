package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.User.ActivateUserRequest;
import delta.codecharacter.server.controller.request.User.PasswordResetRequest;
import delta.codecharacter.server.controller.request.User.RegisterUserRequest;
import delta.codecharacter.server.controller.request.User.UpdateUserRequest;
import delta.codecharacter.server.controller.response.User.PragyanApiResponse;
import delta.codecharacter.server.controller.response.User.PragyanUserDetails;
import delta.codecharacter.server.controller.response.User.PrivateUserResponse;
import delta.codecharacter.server.controller.response.User.PublicUserResponse;
import delta.codecharacter.server.model.PasswordResetDetails;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserActivation;
import delta.codecharacter.server.repository.PasswordResetDetailsRepository;
import delta.codecharacter.server.repository.UserActivationRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.UserAuthUtil.CustomUserDetails;
import delta.codecharacter.server.util.enums.AuthMethod;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private final Logger LOG = Logger.getLogger(UserService.class.getName());

    @Autowired
    UserRatingService userRatingService;

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivationRepository userActivationRepository;

    @Autowired
    private PasswordResetDetailsRepository passwordResetDetailsRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SendGridService sendGridService;

    @Value("${pragyan.event-id}")
    private String pragyanEventId;

    @Value("${pragyan.event-secret}")
    private String pragyanEventSecret;

    @Value("${pragyan.event-login-url}")
    private String pragyanEventLoginUrl;

    /**
     * Register a new User for AuthType MANUAL
     *
     * @param user - User Details from the register Request
     */
    @Transactional
    public void registerUser(@NotNull RegisterUserRequest user) {
        Integer userId = getMaxUserId() + 1;

        User newUser = User.builder()
                .userId(userId)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .password(bCryptPasswordEncoder.encode(user.getPassword()))
                .authMethod(AuthMethod.MANUAL)
                .college(user.getCollege())
                .country(user.getCountry())
                .avatarId(user.getAvatarId() == null ? 1 : Integer.parseInt(user.getAvatarId()))
                .build();

        userRepository.save(newUser);

        // Create initial entry for new user in Leaderboard table
        leaderboardService.initializeLeaderboardData(userId);
        // Create initial entry for new user in UserRating table
        userRatingService.initializeUserRating(userId);
        // Create code repository for the new user
        versionControlService.createCodeRepository(userId);

        sendActivationToken(newUser.getUserId());
    }

    @Transactional
    public User registerPragyanUser(String email, String password, PragyanUserDetails pragyanUserDetails) {
        Integer userId = getMaxUserId() + 1;
        String username = email.split("@")[0];

        User newUser = User.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .fullName(pragyanUserDetails.getFullName())
                .password(bCryptPasswordEncoder.encode(password))
                .country(pragyanUserDetails.getUserCountry())
                .authMethod(AuthMethod.PRAGYAN)
                .isActivated(true)
                .build();

        userRepository.save(newUser);

        // Create initial entry for new user in Leaderboard table
        leaderboardService.initializeLeaderboardData(userId);
        // Create initial entry for new user in UserRating table
        userRatingService.initializeUserRating(userId);
        // Create code repository for new user
        versionControlService.createCodeRepository(userId);

        return newUser;
    }

    /**
     * Register a new User for AuthType SSO
     *
     * @param userDetails User Details provided by the resource server
     */
    @SneakyThrows
    @Transactional
    public void registerOAuthUser(Map<String, String> userDetails) {
        Integer userId = getMaxUserId() + 1;

        String email = userDetails.get("email");
        String name = userDetails.get("name");
        String username = email.split("@")[0];
        if (name == null) name = email.split("@")[0];

        username = getUniqueUsername(username);

        User newUser = User.builder()
                .userId(userId)
                .email(email)
                .fullName(name)
                .username(username)
                .authMethod(AuthMethod.SSO)
                .isActivated(true)
                .build();

        userRepository.save(newUser);

        // Create initial entry for new user in UserRating table
        userRatingService.initializeUserRating(userId);
        // Create initial entry for new user in Leaderboard table
        leaderboardService.initializeLeaderboardData(userId);
        // Create code repository for the new user
        versionControlService.createCodeRepository(userId);
    }

    /**
     * Get unique username from the given username
     *
     * @param username Username sent in the request
     * @return Unique username corresponding to the given username
     */
    private String getUniqueUsername(String username) {
        if (!isUsernamePresent(username))
            return username;

        Integer suffix = 1;
        while (isUsernamePresent(username + suffix))
            suffix++;

        return username + suffix;
    }

    /**
     * Get the public details of the a user
     *
     * @return Public details of the a user
     */
    public PublicUserResponse getPublicUser(String username) {
        User user = userRepository.findByUsername(username);

        return PublicUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .country(user.getCountry())
                .avatarId(user.getAvatarId())
                .build();
    }

    /**
     * Get all details of the authenticated user
     *
     * @return Details of the authenticated user
     */
    public PrivateUserResponse getPrivateUser(Integer userId) {
        User user = userRepository.findByUserId(userId);

        return PrivateUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .college(user.getCollege())
                .country(user.getCountry())
                .userType(user.getUserType())
                .email(user.getEmail())
                .avatarId(user.getAvatarId())
                .isAdmin(user.getIsAdmin())
                .build();
    }

    /**
     * Method called for Fetching UserDetails by the UserDetailsService for Auth
     *
     * @param email Email of the user who is trying to Login
     * @return UserDetails of the user
     */
    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);

        // If the user is not present in DB, check if the user is registered with Pragyan.
        if (user == null) {
            var request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String password = request.getParameter("password");
            PragyanUserDetails pragyanUserDetails = pragyanUserAuth(email, password);
            if (pragyanUserDetails == null)
                return new CustomUserDetails(new User());

            // Add the user registered with Pragyan to DB
            user = registerPragyanUser(email, password, pragyanUserDetails);
            return new CustomUserDetails(user);
        }

        if (user.getAuthMethod().equals(AuthMethod.MANUAL)) {
            return new CustomUserDetails(user);
        }
        if (user.getAuthMethod().equals(AuthMethod.PRAGYAN)) {
            var request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String password = request.getParameter("password"); // get password from request parameter
            PragyanUserDetails pragyanUserDetails = pragyanUserAuth(email, password);
            if (pragyanUserDetails == null) {
                return new CustomUserDetails(new User());
            }
            return new CustomUserDetails(user);
        }

        // AuthType is not PRAGYAN and MANUAL
        throw new Exception("Use Github/Google to Login");
    }

    @SneakyThrows
    private PragyanUserDetails pragyanUserAuth(String email, String password) {

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("user_email", email);
        map.add("user_pass", password);
        map.add("event_id", pragyanEventId);
        map.add("event_secret", pragyanEventSecret);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<String> result = restTemplate.exchange(pragyanEventLoginUrl, HttpMethod.POST, httpEntity, String.class);

        try {
            var userDetailsResponse = gson.fromJson(result.getBody(), PragyanApiResponse.class);
            return userDetailsResponse.getMessage();
        }
        // If credentials are wrong, response will be string instead of type PragyanApiResponse
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Activate the User account by verifying the activation token
     *
     * @param activateUserRequest Activation Details for activating user account
     * @return response message after verifying activation token
     */
    @SneakyThrows
    @Transactional
    public String activateUser(ActivateUserRequest activateUserRequest) {
        UserActivation userActivation = userActivationRepository.findByUserId(activateUserRequest.getUserId());

        if (userActivation == null) {
            return "User Already Activated. Please Login";
        }

        User user = userRepository.findByUserId(userActivation.getUserId());
        if (userActivation.getTokenExpiry().isAfter(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            if (activateUserRequest.getAuthToken().equals(userActivation.getActivationToken())) {
                user.setIsActivated(true);
                userRepository.save(user);
                userActivationRepository.deleteByUserId(user.getUserId());
                return "Account Activation Successful";
            }
            return "Invalid Activation Token";
        }

        // Since token has expired and activation has failed send a new Activation token
        sendActivationToken(user.getUserId());
        return "Activation Token Expired! A new token has been sent to the same email.";
    }

    /**
     * Create an Activation Token and add it to UserActivation table
     *
     * @param userId UserId of the user
     */
    @Transactional
    void sendActivationToken(int userId) {
        UserActivation newUserActivation = UserActivation.builder()
                .userId(userId)
                .activationToken(UUID.randomUUID().toString())
                .tokenExpiry(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusDays(1))
                .build();

        User user = userRepository.findByUserId(userId);

        sendGridService.sendActivationEmail(user.getUserId(), newUserActivation.getActivationToken());

        userActivationRepository.save(newUserActivation);
    }

    /**
     * Create a password reset token and add it to PasswordReset table
     *
     * @param email Email of the user
     */
    @SneakyThrows
    @Transactional
    public void sendPasswordResetLink(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null)
            throw new Exception("Invalid email");

        PasswordResetDetails newPasswordResetDetails = PasswordResetDetails.builder()
                .userId(user.getUserId())
                .tokenExpiry(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusDays(1))
                .passwordResetToken(UUID.randomUUID().toString())
                .build();

        sendGridService.sendPasswordResetEmail(user.getUserId(), newPasswordResetDetails.getPasswordResetToken());

        passwordResetDetailsRepository.save(newPasswordResetDetails);
    }

    /**
     * Update the new password in the User table
     *
     * @param passwordResetRequest Password Reset details from the Change Password Request
     */
    @Transactional
    public String resetPassword(PasswordResetRequest passwordResetRequest) {
        PasswordResetDetails passwordResetDetails = passwordResetDetailsRepository.findByUserId(passwordResetRequest.getUserId());

        if (passwordResetDetails == null)
            return "Invalid User ID";

        if (passwordResetDetails.getTokenExpiry().isAfter(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            if (passwordResetDetails.getPasswordResetToken().equals(passwordResetRequest.getPasswordResetToken())) {
                User user = userRepository.findByUserId(passwordResetDetails.getUserId());

                user.setPassword(bCryptPasswordEncoder.encode(passwordResetRequest.getNewPassword()));
                userRepository.save(user);

                passwordResetDetailsRepository.deleteByUserId(passwordResetDetails.getUserId());
                return "Password reset successful!";
            }
        }

        return "Invalid Password Reset Token / Password Reset Token Expired";
    }

    /**
     * Get the current maximum userId
     *
     * @return Maximum userId
     */
    private Integer getMaxUserId() {
        User user = userRepository.findFirstByOrderByUserIdDesc();
        System.out.println(user);
        if (user == null) {
            return 1;
        }
        return user.getUserId();
    }

    /**
     * Check if there is a User for an email
     *
     * @param email Email of the user
     * @return True if the email exists, False if it doesn't
     */
    public boolean isEmailPresent(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    /**
     * Check if there is a User for the given username
     *
     * @param username Username of the user
     * @return True if the username exists, False if it doesn't
     */
    public boolean isUsernamePresent(String username) {
        User user = userRepository.findByUsername(username);
        return user != null;
    }

    /**
     * Get user by email
     *
     * @param email Email of user
     * @return User with the given email
     */
    @SneakyThrows
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    /**
     * Check if user is admin
     *
     * @param email Email of user to be checked
     * @return True if user is admin, else False
     */
    @SneakyThrows
    public boolean getIsAdminByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return (user != null) && (user.getIsAdmin());
    }

    /**
     * Update a user's details
     *
     * @param email             User's email
     * @param updateUserRequest User Details from the updateUserRequest
     */
    @SneakyThrows
    @Transactional
    public void updateUser(String email, @NotNull UpdateUserRequest updateUserRequest) {
        User user = getUserByEmail(email);

        if (isUsernamePresent(updateUserRequest.getUsername()))
            throw new Exception("Username already exists");

        User newUser = User.builder()
                .userId(user.getUserId())
                .username(updateUserRequest.getUsername() == null ? user.getUsername() : updateUserRequest.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .userType(updateUserRequest.getUserType() == null ? user.getUserType() : updateUserRequest.getUserType())
                .authMethod(user.getAuthMethod())
                .fullName(updateUserRequest.getFullName() == null ? user.getFullName() : updateUserRequest.getFullName())
                .college(updateUserRequest.getCollege() == null ? user.getCollege() : updateUserRequest.getCollege())
                .country(updateUserRequest.getCountry() == null ? user.getCountry() : updateUserRequest.getCountry())
                .avatarId(updateUserRequest.getAvatarId() == null ? user.getAvatarId() : Integer.parseInt(updateUserRequest.getAvatarId()))
                .build();

        userRepository.save(newUser);
    }

    /**
     * Change a user's password
     *
     * @param email    - user's email
     * @param password - new password of user
     */
    @SneakyThrows
    @Transactional
    public void updatePassword(String email, String password) {
        User user = getUserByEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
    }

    /**
     * Get email of currently authenticated user
     *
     * @param authentication Authentication details of the logged in user
     * @return Email of the logged in user
     */
    public String getEmailFromAuthentication(Authentication authentication) {
        if (authentication instanceof OAuth2Authentication) {
            var userDetails = ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
            Map<String, String> userDetailsMap = (Map<String, String>) userDetails;

            return userDetailsMap.get("email");
        }
        return authentication.getName();
    }
}
