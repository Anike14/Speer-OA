package com.speer.OA.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.speer.OA.models.User;
import com.speer.OA.repositories.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	
	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserDetailsService userDetailsService() {
	    return new UserDetailsService() {
	        @Override
	        public User loadUserByUsername(String username) {
	            User user = userRepository.findByUsername(username);
	            if (user == null) throw new UsernameNotFoundException("User not found");
				return user;
	        }
	    };
	}
}
