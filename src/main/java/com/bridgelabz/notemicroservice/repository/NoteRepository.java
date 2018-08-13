package com.bridgelabz.notemicroservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bridgelabz.notemicroservice.model.Note;


public interface NoteRepository extends MongoRepository<Note, String> {

	public List<Note> findAllByUserId(String userId);

	public Optional<Note> findByUserId(String userId);

	public List<Note> findAllByUserIdAndTrash(String userId, boolean isTrashed);

	public Optional<Note> findByNoteIdAndUserId(String noteId, String userId);
}