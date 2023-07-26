package com.speer.OA.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @ManyToMany
    @JoinTable(
            name = "note_viewers",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "editor_id")
    )
    private List<User> viewers;
    
    @ManyToMany
    @JoinTable(
            name = "note_editors",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "editor_id")
    )
    private List<User> editors;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
		this.addEditor(creator);
	}

	public List<User> getViewers() {
		return viewers;
	}
	
	public void addViewer(User viewer) {
		if (this.viewers == null)
			this.viewers = new ArrayList<>();
		this.viewers.add(viewer);
	}
	
	public List<User> getEditors() {
		return editors;
	}
	
	public void addEditor(User editor) {
		if (this.editors == null)
			this.editors = new ArrayList<>();
		this.editors.add(editor);
		this.addViewer(editor);
	}
}

