package com.speer.OA.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.speer.OA.models.User;
import com.speer.OA.repositories.UserRepository;
import com.speer.OA.security.JWTService;

public class UserControllerTest {
	
	private AutoCloseable closeable;
	
	@Mock
    private JWTService jwtService;
	
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    // this is the target that accept the test
    private UserController userController;
    
    User testSubject;
    
    @BeforeEach
    public void setUp() {
        // Create a user for testing
    	testSubject = new User();
    	testSubject.setId(1000L);
    	testSubject.setUsername("Tester1");
    	testSubject.setPassword("Tester1Password");

    	// scan and init beans for @Mock
    	closeable = MockitoAnnotations.openMocks(this);
    	userController = new UserController(jwtService, userRepository);
    }
    
    @AfterEach
    public void closeService() throws Exception {
        closeable.close();
    }
    
    // we still need more test case, but for now all endpoints under /auth/ has at least one test case.
    
    @Test
    public void testSignUpUser_Success() {
    	// Try signing up
    	when(userRepository.save(any(User.class))).thenReturn(testSubject);
    	ResponseEntity<String> response = userController.signUpUser(testSubject);

        // Verify that the response entity has status code 200 (OK)
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User signed up", response.getBody());
    }

    @Test
    public void testSignUpUser_Failure() {
        // test data
        User testSubject = new User();
        testSubject.setUsername("Tester1");
        testSubject.setPassword("Tester1Password");
        
    	when(userRepository.save(any(User.class))).thenThrow(new RuntimeException());
        ResponseEntity<String> response = userController.signUpUser(testSubject);

        // Verify that the response entity is not status code 200 (OK)
        assertNotEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testLoginUser_Success() {
        // Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject.getUsername())).thenReturn(testSubject);

        // Mock the jwtService to return a valid access token
        when(jwtService.generateAccessToken(testSubject.getUsername())).thenReturn("validAccessToken");

        User userRequest = new User();
        userRequest.setUsername(testSubject.getUsername());
        userRequest.setPassword(testSubject.getPassword());

        ResponseEntity<String> responseEntity = userController.loginUser(userRequest);

        // Verify that the response entity contains the access token and has status code 200 (OK)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(jwtService.generateAccessToken(testSubject.getUsername()), responseEntity.getBody());
    }

    @Test
    public void testLoginUser_Failure() {
    	// Mock the scenario that user not found via findByUsername
        when(userRepository.findByUsername(testSubject.getUsername())).thenReturn(null);

        User userRequest = new User();
        userRequest.setUsername(testSubject.getUsername());
        userRequest.setPassword(testSubject.getPassword());

        ResponseEntity<String> responseEntity = userController.loginUser(userRequest);

        // Verify that the response entity is not status code 200 (OK)
        assertNotEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
