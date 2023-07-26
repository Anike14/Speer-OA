package com.speer.OA.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.speer.OA.models.Note;
import com.speer.OA.models.User;
import com.speer.OA.repositories.NoteRepository;
import com.speer.OA.repositories.UserRepository;
import com.speer.OA.security.JWTService;

@ExtendWith(MockitoExtension.class)
public class NoteControllerTest {
	
	private AutoCloseable closeable;
	
	@Mock
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteController noteController;

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
        
        // scan and init beans for @Mock
    	closeable = MockitoAnnotations.openMocks(this);
    	noteController = new NoteController(userRepository, noteRepository);
    }
    
    @AfterEach
    public void closeService() throws Exception {
        closeable.close();
    }
    
    // we still need more test case, but for now all endpoints under /notes/ has at least one test case.

    @Test
    public void testGetNotesByAuthenticatedUser() {
        // Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        
        // Mock the scenario that the user can view all our notes
        when(noteRepository.viewersContains(testSubject1)).thenReturn(notes);
        ResponseEntity<?> responseEntity = noteController.getNotesByAuthenticatedUser(testSubject1);

        // Verify that the response entity contains the list of notes and has status code 200 (OK)
        assertEquals(notes, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @Test
    public void testGetNoteById() {
        // Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        
        // Mock the scenario note1 is found
        when(noteRepository.getNoteById(note1.getId())).thenReturn(note1);
        ResponseEntity<?> responseEntity = noteController.getNoteById(note1.getId(), testSubject1);

        // Verify that the response entity contains note1 and has status code 200 (OK)
        assertEquals(note1, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @Test
    public void testCreateNote() {
        // Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        
        // Create a new note to be created
        Note newNote = new Note();
        newNote.setContent("Test note content");
        newNote.setCreator(testSubject1);
        
        // Mock the scenario that the new note is save to the database
        when(noteRepository.save(newNote)).thenReturn(newNote);
        ResponseEntity<?> responseEntity = noteController.createNote(newNote, testSubject1);
        
        // Verify that the response entity has status code 201 (Created)
        assertEquals("Note created!", responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @Test
    public void testUpdateNote() {
        // Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);

        note1.setContent("This is Note 1 updated!");

        // Mock the scenario note1 is found
        when(noteRepository.getNoteById(note1.getId())).thenReturn(note1);
        
        // Mock the scenario that the updated note is save to the database
        when(noteRepository.save(note1)).thenReturn(note1);
        ResponseEntity<?> responseEntity = noteController.updateNote(note1.getId(), note1, testSubject1);
        
        // Verify that the response entity contains has status code 200 (OK)
        assertEquals("Note updated!", responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @Test
    public void testDeleteNote() {
    	// Mock the scenario that user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        
        // Mock the scenario note1 is found
        when(noteRepository.getNoteById(note1.getId())).thenReturn(note1);
        
        // Mock the scenario that note1 is deleted from the database
        ResponseEntity<?> responseEntity = noteController.deleteNote(note1.getId(), testSubject1);
        
        // Verify that the response entity contains has status code 200 (OK)
        assertEquals("Note deleted!", responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
    @Test
    public void testShareNote() {
    	// Mock the scenario that both users exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        when(userRepository.findByUsername(testSubject2.getUsername())).thenReturn(testSubject2);

        // Mock the scenario note1 is found
        when(noteRepository.getNoteById(note1.getId())).thenReturn(note1);
        
        // Mock the scenario that note1 has been shared to testSubject2 by testSubject1
        ResponseEntity<String> responseEntity = noteController.shareNote(note1.getId(), testSubject2, testSubject1);
        
        // Verify that the response entity has status code 200 (OK)
        assertEquals("Note shared!", responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Verify that the viewers list of note1 contains the target user
        assertEquals(true, noteRepository.getNoteById(note1.getId()).getViewers().contains(testSubject2));
    }
    
    @Test
    public void testSearchNotes() {
    	// Mock the scenario that the user does exist in our database
        when(userRepository.findByUsername(testSubject1.getUsername())).thenReturn(testSubject1);
        
        // Mock the scenario that note1 is the only matched record in our database
        String query = "Note 1";
        List<Note> foundNotes = new ArrayList<>();
        foundNotes.add(note1);
        when(noteRepository.findByContentContainingAndViewersContains(query, testSubject1)).thenReturn(foundNotes);
        
        // Mock the scenario that we trigger the search
        ResponseEntity<?> responseEntity = noteController.searchNotes(query, testSubject1);
        
        // Verify that the response entity contains the list of found notes and has status code 200 (OK)
        assertEquals(foundNotes, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
