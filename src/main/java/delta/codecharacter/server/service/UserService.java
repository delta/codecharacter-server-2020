package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.User.PasswordResetRequest;
import delta.codecharacter.server.controller.request.User.PublicUserRequest;
import delta.codecharacter.server.controller.request.User.RegisterUserRequest;
import delta.codecharacter.server.controller.response.PragyanUserDetailsResponse;
import delta.codecharacter.server.model.PasswordResetDetails;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserActivation;
import delta.codecharacter.server.repository.PasswordResetDetailsRepository;
import delta.codecharacter.server.repository.UserActivationRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.MailTemplate;
import delta.codecharacter.server.util.UserAuthUtil.CustomUserDetails;
import delta.codecharacter.server.util.enums.AuthMethod;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;

@Service
public class UserService implements UserDetailsService {

    private final Logger LOG = Logger.getLogger(UserService.class.getName());

    @Autowired
    UserRatingService userRatingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivationRepository userActivationRepository;

    @Autowired
    private PasswordResetDetailsRepository passwordResetDetailsRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${api.pragyan.event-id}")
    private String pragyanApiEventId;

    @Value("${api.pragyan.event-secret}")
    private String pragyanApiEventSecret;

    @Value("${api.pragyan.event-login-url}")
    private String pragyanEventUrl;

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

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

        sendActivationToken(newUser.getUserId());
    }

    @Transactional
    public User registerPragyanUser(@NotNull RegisterUserRequest user) {
        Integer userId = getMaxUserId() + 1;

        User newUser = User.builder()
                .userId(userId)
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        userRepository.save(newUser);
        leaderboardService.initializeLeaderboardData(userId);

        //create initial entry for new user in UserRating table
        userRatingService.initializeUserRating(userId);

        sendActivationToken(newUser.getUserId());
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
    }

    /**
     * Get the details of all the Users
     *
     * @return Details of all Users
     */
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

    /**
     * Method called for Fetching UserDetails by the UserDetailsService for Auth
     *
     * @param email Email of the user who is trying to Login
     * @return UserDetails of the user
     */
    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String password = request.getParameter("password"); // get from request parameter

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("user_email", email);
        map.add("user_pass", password);
        map.add("event_id", pragyanApiEventId);
        map.add("event_secret", pragyanApiEventSecret);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<String> result = restTemplate.exchange(pragyanEventUrl, HttpMethod.POST, httpEntity, String.class);

        PragyanUserDetailsResponse userDetailsResponse = gson.fromJson(result.getBody(), PragyanUserDetailsResponse.class);

        User user = userRepository.findByEmail(email);

        if (userDetailsResponse.getStatusCode().equals(200)) {
            if (user == null) {
                String username = email.split("@")[0];

                RegisterUserRequest registerUserRequest = RegisterUserRequest.builder()
                        .email(email)
                        .username(username)
                        .build();

                user = registerPragyanUser(registerUserRequest);
            }
            return new CustomUserDetails(user);
        }

        if (user == null)
            throw new Exception("Unauthorized");

        //Check AuthType
        if (user.getAuthMethod().equals(AuthMethod.MANUAL)) {
            if (!user.getIsActivated()) throw new Exception("User not activated");
            return new CustomUserDetails(user);
        }

        //AuthType is not PRAGYAN and MANUAL
        throw new Exception("Use Github/Google to Login");
    }

    /**
     * Activate the User account by verifying the activation token
     *
     * @param activationToken Activation Token received from the Request
     */
    @SneakyThrows
    @Transactional
    public void activateUser(String activationToken) {
        UserActivation userActivation = userActivationRepository.findByActivationToken(activationToken);

        if (userActivation == null) {
            throw new Exception("User Already Activated. Please Login");
        }

        User user = userRepository.findByUserId(userActivation.getUserId());

        if (userActivation.getTokenExpiry().isAfter(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))))
            if (activationToken.equals(userActivation.getActivationToken())) {
                user.setIsActivated(true);
                userRepository.save(user);
                userActivationRepository.deleteByUserId(user.getUserId());
                return;
            }

        //Since Activation has failed send a new Activation token
        sendActivationToken(user.getUserId());

        throw new Exception("Invalid Activation Token / Activation Token Expired");
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

        javaMailSender.send(MailTemplate.getActivationMessage(user.getEmail(), user.getUsername(), newUserActivation.getActivationToken()));

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

        javaMailSender.send(MailTemplate.getPasswordResetMessage(user.getEmail(), user.getUsername(), newPasswordResetDetails.getPasswordResetToken()));

        passwordResetDetailsRepository.save(newPasswordResetDetails);
    }

    /**
     * Update the new password in the User table
     *
     * @param passwordResetRequest Password Reset details from the Change Password Request
     */
    @SneakyThrows
    @Transactional
    public void changePassword(PasswordResetRequest passwordResetRequest) {
        PasswordResetDetails passwordResetDetails = passwordResetDetailsRepository.findByPasswordResetToken(passwordResetRequest.getPasswordResetToken());

        if (passwordResetDetails == null)
            throw new Exception("Invalid Token");

        if (passwordResetDetails.getTokenExpiry().isAfter(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            if (passwordResetDetails.getPasswordResetToken().equals(passwordResetRequest.getPasswordResetToken())) {
                User user = userRepository.findByUserId(passwordResetDetails.getUserId());

                user.setPassword(bCryptPasswordEncoder.encode(passwordResetRequest.getNewPassword()));
                userRepository.save(user);

                passwordResetDetailsRepository.deleteByUserId(passwordResetDetails.getUserId());
                return;
            }
        }

        throw new Exception("Invalid Password Reset Token / Password Reset Token Expired");
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

    @SneakyThrows
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    @SneakyThrows
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }
        return user;
    }

    @SneakyThrows
    public boolean getIsAdminUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return (user != null) && (user.getIsAdmin());
    }

    public String getEmailFromAuthentication(Authentication authentication) {
        if (authentication instanceof OAuth2Authentication) {
            var userDetails = ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
            Map<String, String> userDetailsMap = (Map<String, String>) userDetails;

            return userDetailsMap.get("email");
        }
        return authentication.getName();
    }
}
