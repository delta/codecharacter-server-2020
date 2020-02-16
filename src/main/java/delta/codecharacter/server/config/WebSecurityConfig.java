package delta.codecharacter.server.config;

import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.util.UserAuthUtil.ClientResources;
import delta.codecharacter.server.util.UserAuthUtil.CustomAuthProcessingFilter;
import delta.codecharacter.server.util.UserAuthUtil.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Configuration
@EnableOAuth2Client
@EnableWebSecurity
@RestController
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger LOG = Logger.getLogger(WebSecurityConfig.class.getName());
    String[] ignoringAntMatchers = {"/", "/login/**", "/error/**", "/logout", "/user", "/user/activate", "/user/forgot-password", "/user/password"};

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Qualifier("oauth2ClientContext")
    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    @Autowired
    private UserService userService;

    //Configures where to fetch the user from
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }

    //Prevent unauthenticated access and also exclude specified end-point
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests().antMatchers(ignoringAntMatchers).permitAll().anyRequest().authenticated().and()
                .exceptionHandling().and()
                .formLogin().loginPage("/login").usernameParameter("email").failureHandler(new CustomAuthenticationFailureHandler()).and()
                .logout().logoutSuccessUrl("/").and()
                .csrf().ignoringAntMatchers(ignoringAntMatchers).csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
                .addFilterBefore(ssoFilters(), BasicAuthenticationFilter.class);
    }

    //Fetch data present in application.properties
    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.github")
    public ClientResources github() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.oauth2.client.google")
    public ClientResources google() {
        return new ClientResources();
    }

    //Create Filters to handle Sso
    private Filter ssoFilters() {
        var filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(createSsoFilter(github(), "/login/github"));
        filters.add(createSsoFilter(google(), "/login/google"));
        filter.setFilters(filters);
        return filter;
    }

    //Map the respective filter with its data
    private Filter createSsoFilter(ClientResources client, String path) {
        var customAuthProcessingFilter = new CustomAuthProcessingFilter(path, userService);
        var oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        customAuthProcessingFilter.setRestTemplate(oAuth2RestTemplate);
        var tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
                client.getClient().getClientId());
        tokenServices.setRestTemplate(oAuth2RestTemplate);
        customAuthProcessingFilter.setTokenServices(tokenServices);
        return customAuthProcessingFilter;
    }

    //Register the Filters created above
    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        var registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
