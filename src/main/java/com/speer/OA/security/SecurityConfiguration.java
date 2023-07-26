package com.speer.OA.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    
	private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    public SecurityConfiguration(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
    	this.userService = userService;
    	this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http.csrf(AbstractHttpConfigurer::disable);

    	// indicating all paths with "/auth" are bypassing the jwtAuthenticationFilter
    	http.authorizeHttpRequests((request)->request.requestMatchers("/auth/**").permitAll());
    	// indicating all paths with "/notes" are going into the jwtAuthenticationFilter
    	http.authorizeHttpRequests((request)->request.requestMatchers("/notes/**").authenticated());
    	
    	// because we need to do JWT authentication, so we need to set session manager to stateless
    	http.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS));
    	// add jwtAuthenticationFilter before we validate the username password filter is activated
        http.authenticationProvider(authenticationProvider())
        	.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        SecurityFilterChain filterChain = http.build();
        return filterChain;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
