package delta.codecharacter.server.util.UserAuthUtil;

import delta.codecharacter.server.service.UserService;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

//Override the default method to intercept data from Resource Server and register the User
public class CustomAuthProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

    Logger LOG = Logger.getLogger(CustomAuthProcessingFilter.class.getName());

    UserService userService;

    public CustomAuthProcessingFilter(String defaultFilterProcessesUrl, UserService userService) {
        super(defaultFilterProcessesUrl);
        this.userService = userService;
    }

    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        var userDetails = ((OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication()).getUserAuthentication().getDetails();
        Map<String, String> userDetailsMap = (Map<String, String>) userDetails;

        //If email is not present, throw an exception
        if (userDetailsMap.get("email") == null) {
            SecurityContextHolder.clearContext();
            throw new Exception("Please Update Public Email");
        }

        String username = userDetailsMap.get("login");
        if (username == null) username = userDetailsMap.get("email").split("@")[0];

        //If email is present, the user can be logged in
        if (userService.isEmailPresent(userDetailsMap.get("email"))) {
            super.successfulAuthentication(request, response, chain, authResult);
            return;
        }

        //If email is not present but username is present, throw an exception
        if (userService.isUsernamePresent(username)) {
            SecurityContextHolder.clearContext();
            throw new Exception("Username Already Taken");
        }

        //If both email and username are not present, register the User
        userService.registerOAuthUser(userDetailsMap);
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
