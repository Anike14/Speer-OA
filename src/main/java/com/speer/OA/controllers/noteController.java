package com.speer.OA.controllers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.speer.OA.models.Note;
import com.speer.OA.models.User;
import com.speer.OA.repositories.NoteRepository;
import com.speer.OA.repositories.UserRepository;
import com.speer.OA.security.RateLimitConfiguration;

import io.github.bucket4j.Bucket;


@Controller
public class NoteController {

	private final UserRepository userRepository;
	private final NoteRepository noteRepository;
	
	private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();
	
	@Autowired
	public NoteController(UserRepository userRepository, NoteRepository noteRepository) {
	    this.userRepository = userRepository;
	    this.noteRepository = noteRepository;
	}
	
	private boolean getBucket(Long userId) {
		Bucket bucket = buckets.computeIfAbsent(userId, key -> RateLimitConfiguration.createNewBucket());
        if (bucket.tryConsume(1)) return true;
        else return false;
	}
	
	@GetMapping("/notes")
	public ResponseEntity<?> getNotesByAuthenticatedUser(@AuthenticationPrincipal User user) {
		try {
			User existingUser = userRepository.findByUsername(user.getUsername());
			if (!getBucket(existingUser.getId())) 
				return new ResponseEntity<>(
					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
	        List<Note> notes = noteRepository.viewersContains(existingUser);
	        return new ResponseEntity<>(notes, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<>(
					"Failed to fetch data. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable Long noteId, @AuthenticationPrincipal User user) {
    	try {
    		User existingUser = userRepository.findByUsername(user.getUsername());
			if (!getBucket(existingUser.getId())) 
				return new ResponseEntity<>(
					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
			Note existingNote = noteRepository.getNoteById(noteId);
			if (!existingNote.getViewers().contains(existingUser)) {
 	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this note.");
 	        }
	        return new ResponseEntity<>(existingNote, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<>(
					"Failed to fetch data. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @PostMapping("/notes")
    public ResponseEntity<String> createNote(@RequestBody Note note, @AuthenticationPrincipal User user) {
    	 try {
    		 User existingUser = userRepository.findByUsername(user.getUsername());
 			 if (!getBucket(existingUser.getId())) 
 				return new ResponseEntity<>(
 					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
 			 note.setCreator(existingUser);
    		 noteRepository.save(note);
             return new ResponseEntity<>("Note created!", HttpStatus.OK);
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Failed to create a note, please try again later");
         }
    }
    
    @PutMapping("/notes/{noteId}")
    public ResponseEntity<String> updateNote(@PathVariable Long noteId, @RequestBody Note note, @AuthenticationPrincipal User user) {
    	 try {
    		 User existingUser = userRepository.findByUsername(user.getUsername());
 			 if (!getBucket(existingUser.getId())) 
 				return new ResponseEntity<>(
 					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
 			 Note existingNote = noteRepository.getNoteById(noteId);
 			 if (existingNote == null) {
 				 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
 	                     .body("note not found, please double check your note id.");
 	         }
 	         if (!existingNote.getViewers().contains(existingUser)) {
 	             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this note.");
 	         }
 			 existingNote.setContent(note.getContent());
 			 noteRepository.save(existingNote);
             return new ResponseEntity<>("Note updated!", HttpStatus.OK);
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Failed to update the note, please try again later");
         }
    }
    
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteId, @AuthenticationPrincipal User user) {
    	try {
   		 	User existingUser = userRepository.findByUsername(user.getUsername());
			if (!getBucket(existingUser.getId())) 
				return new ResponseEntity<>(
					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
			Note existingNote = noteRepository.getNoteById(noteId);
			if (existingNote == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                     .body("note not found, please double check your note id.");
	        }
	        if (existingNote.getCreator().getId() != existingUser.getId()) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this note.");
	        }
			noteRepository.delete(existingNote);
            return new ResponseEntity<>("Note deleted!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete the note, please try again later");
        }
    }
    
    @PostMapping("/notes/{noteId}/share")
    public ResponseEntity<String> shareNote(@PathVariable Long noteId, @RequestBody User target, @AuthenticationPrincipal User user) {
    	try {
   		 	User existingUser = userRepository.findByUsername(user.getUsername());
			if (!getBucket(existingUser.getId())) 
				return new ResponseEntity<>(
					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
			
			Note existingNote = noteRepository.getNoteById(noteId);
			if (existingNote == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                     .body("Note not found, please double check your note id.");
	        }
	        if (!existingNote.getViewers().contains(existingUser)) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to view this note.");
	        }
	        
	        User targetUser = userRepository.findByUsername(target.getUsername());
	        if (targetUser == null) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                     .body("The user you are sharing to does not existed, please double check your target id.");
	        }
	        existingNote.addEditor(targetUser);
			noteRepository.save(existingNote);
            return new ResponseEntity<>("Note shared!", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to share the note, please try again later");
        }
    }
    
    @GetMapping("/notes/search")
    public ResponseEntity<?> searchNotes(@RequestParam(value="q", required=true) String query, @AuthenticationPrincipal User user) {
    	try {
    		User existingUser = userRepository.findByUsername(user.getUsername());
			if (!getBucket(existingUser.getId())) 
				return new ResponseEntity<>(
					"Too Many Requests sent, please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    		
    		List<Note> foundNotes = noteRepository.findByContentContainingAndViewersContains(query, user);
	        return ResponseEntity.ok(foundNotes);
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search, please try again later");
        }
    }
}
