package delta.codecharacter.server.util.UserAuthUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        Map<String, String> res = new HashMap<String, String>();
        res.put("message", "Authenticated");
        res.put("status", "200");

        httpServletResponse.getWriter().write(new ObjectMapper().writeValueAsString(res));
        httpServletResponse.setStatus(200);
    }
}
