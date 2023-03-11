package com.ph.rest.webservices.restfulwebservices;


import com.ph.service.MD5PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig  extends WebSecurityConfigurerAdapter {
    private UserDetailsService userDetailsService;

    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/view/login",
                    "/forgot",
                    "/",
                    "/persons/**",
                    "/absences/**",
                    "/events/**",
                    "/users/**",
                    "/auth/**",
                    "/register/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/view/home")
            .failureUrl("/login?error")
            .permitAll()
            .and()
            .logout()
                .logoutUrl("/perform_logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            .permitAll().and().csrf().disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new MD5PasswordEncoder());
    }
}