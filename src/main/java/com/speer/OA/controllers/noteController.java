package com.speer.OA.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.speer.OA.models.Note;
import com.speer.OA.models.User;
import com.speer.OA.repositories.NoteRepository;
import com.speer.OA.repositories.UserRepository;


@Controller
public class noteController {

	private final UserRepository userRepository;
	private final NoteRepository noteRepository;
	
	@Autowired
	public noteController(UserRepository userRepository, NoteRepository noteRepository) {
	    this.userRepository = userRepository;
	    this.noteRepository = noteRepository;
	}
	
	@GetMapping("/notes")
	public ResponseEntity<?> getNotesByAuthenticatedUser(@AuthenticationPrincipal User user) {
		try {
			User existingUser = userRepository.findByUsername(user.getUsername());
	        List<Note> notes = noteRepository.findByCreatorId(existingUser.getId());
	        return new ResponseEntity<>(notes, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<>(
					"Failed to fetch data. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId, @AuthenticationPrincipal User user) {
    	try {
			Note note = noteRepository.getNoteById(noteId);
	        return new ResponseEntity<>(note, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<>(
					"Failed to fetch data. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    
}
