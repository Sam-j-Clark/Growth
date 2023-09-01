package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.authentication.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * The Spring Security configuration for the portfolio application.
 *
 * This class defines how to handle authentication of users in the LensFolio application, as well as
 * declaring the request endpoints which can bypass the security requirements.
 *
 * Changed 04/08/2022 due to deprecation of WebSecurityConfigurerAdapter.
 * @see <a href="https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter">
 *     WebSecurityConfigurerAdapter deprecation Blog
 *     </a>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Registers a SecurityFilterChain Bean to handle the Spring Configuration of the authentication process.
     *
     * @return The registered SecurityFilterChain Bean with the security configuration.
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        // Force authentication for all endpoints except /login
        String loginAntPattern = "/login";
        security
                .addFilterBefore(new JwtAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, loginAntPattern)
                .permitAll()
                .antMatchers(HttpMethod.GET, "/register")
                .permitAll()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();


        security.cors();
        security.csrf().disable();
        security.logout()
                .logoutSuccessUrl(loginAntPattern)
                .permitAll()
                .invalidateHttpSession(true)
                .deleteCookies("lens-session-token");

        security.exceptionHandling().accessDeniedPage("/login.html");

        // Disable basic http security
        security
                .httpBasic().disable();


        // Tells spring where our login page is, so it redirects users there if they are not authenticated
        security.formLogin().loginPage(loginAntPattern);
        return security.build();
    }


    /**
     * Registers a WebSecurityCustomizer Bean to declared requests which ignore the web security.
     *
     * @return The registered WebSecurityCustomizer Bean with the paths to ignore.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .antMatchers("/login")
                .antMatchers("/register")
                .antMatchers("/bootstrap/**")
                .antMatchers("/js/**")
                .antMatchers("/css/**");
    }


}