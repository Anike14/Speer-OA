package com.speer.OA.repositories;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.speer.OA.models.Note;
import com.speer.OA.models.User;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
	public List<Note> viewersContains(User viewer);
	public Note getNoteById(Long noteID);
	public List<Note> findByContentContainingAndViewersContains(String query, User user);
}

