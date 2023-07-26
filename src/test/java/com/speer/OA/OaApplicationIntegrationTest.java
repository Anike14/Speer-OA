package com.speer.OA;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.speer.OA.models.Note;
import com.speer.OA.models.User;
import com.speer.OA.repositories.NoteRepository;
import com.speer.OA.repositories.UserRepository;

//The Integration Test class is not yet finished
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OaApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private NoteRepository noteRepository;

    private User testSubject1;
    private User testSubject2;
    private List<Note> notes = new ArrayList<>();
    private Note note1;
    private Note note2;

    @BeforeEach
    public void setUp() {
    	testSubject1 = new User();
    	testSubject1.setId(1000L);
    	testSubject1.setUsername("Tester1");
    	testSubject1.setPassword("Tester1Password");
    	
    	testSubject2 = new User();
    	testSubject1.setId(2000L);
    	testSubject2.setUsername("Tester2");
    	testSubject2.setPassword("Tester2Password");

        note1 = new Note();
        note1.setId(1L);
        note1.setContent("This is Note 1");
        note1.setCreator(testSubject1);
        notes.add(note1);

        note2 = new Note();
        note2.setId(2L);
        note2.setContent("This is Note 2");
        note2.setCreator(testSubject1);
        notes.add(note2);
    }
    
    @Test
    public void testGetNotesByAuthenticatedUser() throws Exception {
    	// Mock that a GET request is send to our "/notes" endpoint
    	MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .with(request -> {
                    request.setRemoteUser(testSubject1.getUsername());
                    return request;
                }))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    	
    	String responseContent = result.getResponse().getContentAsString();
        List<Note> notes = Arrays.asList(objectMapper.readValue(responseContent, Note[].class));

        // Verify the result is the same with our mocked data
        assertThat(notes).hasSize(2);
        assertThat(notes.get(0).getCreator()).isEqualTo(testSubject1);
        assertThat(notes.get(1).getCreator()).isEqualTo(testSubject1);
    }
}