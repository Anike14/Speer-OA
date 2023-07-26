package com.speer.OA.repositories;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.speer.OA.models.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
	public List<Note> findByCreatorId(Long userId);
	public Note getNoteById(Long noteID);
}

