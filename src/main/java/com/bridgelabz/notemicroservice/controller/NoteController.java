package com.bridgelabz.notemicroservice.controller;

import java.text.ParseException;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.notemicroservice.exceptions.LinkInformationException;
import com.bridgelabz.notemicroservice.exceptions.InvalidLabelNameException;
import com.bridgelabz.notemicroservice.exceptions.LabelException;
import com.bridgelabz.notemicroservice.exceptions.LabelNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.NoteException;
import com.bridgelabz.notemicroservice.exceptions.NoteNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.ReminderException;
import com.bridgelabz.notemicroservice.exceptions.UnauthorizedException;
import com.bridgelabz.notemicroservice.model.CreateNoteDTO;
import com.bridgelabz.notemicroservice.model.NoteDTO;
import com.bridgelabz.notemicroservice.model.Response;
import com.bridgelabz.notemicroservice.model.UpdateNote;
import com.bridgelabz.notemicroservice.service.NoteService;

@RestController
@RequestMapping("/note")
public class NoteController {

	@Autowired
	private NoteService noteService;

	/**
	 * to create a new note
	 * 
	 * @param request
	 * @param newNote
	 * @return NoteDTO
	 * @throws NoteException
	 * @throws ReminderException
	 * @throws ParseException
	 * @throws LinkInformationException
	 */
	@PostMapping(value = "/create")
	public ResponseEntity<NoteDTO> createNote(HttpServletRequest request, @RequestBody CreateNoteDTO newNote)
			throws NoteException, ReminderException, LinkInformationException {

		String userId = request.getHeader("userId");
		NoteDTO noteDto = noteService.createNote(newNote, userId);
		return new ResponseEntity<>(noteDto, HttpStatus.CREATED);
	}

	/**
	 * open a note with given note Id
	 * 
	 * @param request
	 * @param noteId
	 * @return NoteDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws LinkInformationException
	 */
	@GetMapping(value = "/getNote/{noteId}")
	public ResponseEntity<NoteDTO> getNote(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException, LinkInformationException {

		String userId = request.getHeader("userId");
		NoteDTO noteDto = noteService.viewNote(userId, noteId);

		return new ResponseEntity<>(noteDto, HttpStatus.OK);
	}

	/**
	 * Open all note of user
	 * 
	 * @param request
	 * @return List of notes
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 */
	@GetMapping(value = "/getAllNotes")
	public ResponseEntity<List<NoteDTO>> getAllNotes(HttpServletRequest request)
			throws NoteNotFoundException, LinkInformationException {

		String userId = request.getHeader("userId");

		List<NoteDTO> noteList = noteService.viewAllNotes(userId);
		noteList.sort(Comparator.comparing(NoteDTO::getTitle).reversed());
		return new ResponseEntity<>(noteList, HttpStatus.OK);
	}

	/**
	 * Update a note
	 * 
	 * @param request
	 * @param noteId
	 * @param updateNote
	 * @return ResponseDTO
	 * @throws NoteException
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws ReminderException
	 * @throws ParseException
	 */
	@PutMapping(value = "/update/{noteId}")
	public ResponseEntity<Response> updateNote(HttpServletRequest request, @PathVariable String noteId,
			@RequestBody UpdateNote updateNote)
			throws NoteException, NoteNotFoundException, UnauthorizedException, ReminderException {

		String userId = request.getHeader("userId");
		noteService.updateNote(updateNote, userId, noteId);

		Response response = new Response();
		response.setMessage("Note Successfully updated");
		response.setStatus(91);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * to move a note to trash
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/delete/{noteId}")
	public ResponseEntity<Response> deleteNote(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.deleteNote(userId, noteId);

		Response response = new Response();
		response.setMessage("Note Successfully moved to trash");
		response.setStatus(92);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * delete note from trash
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@DeleteMapping(value = "/permanentDeleteRestore/{noteId}")
	public ResponseEntity<Response> deleteFromTrash(HttpServletRequest request, @PathVariable String noteId,
			@RequestParam boolean delete) throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.permanentNoteDelete(userId, noteId, delete);

		Response response = new Response();
		response.setMessage("Note deleted permanently");
		response.setStatus(93);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * to empty the trash notes
	 * 
	 * @param request
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 */
	@DeleteMapping(value = "/emptyTrash")
	public ResponseEntity<Response> emptyTrash(HttpServletRequest request) throws NoteNotFoundException {

		String userId = request.getHeader("userId");
		noteService.emptyTrash(userId);

		Response response = new Response();
		response.setMessage("Trash is emptied");
		response.setStatus(94);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * To view trash notes
	 * 
	 * @param request
	 * @return list of trash notes
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 */
	@GetMapping(value = "/getTrash")
	public ResponseEntity<List<NoteDTO>> getTrash(HttpServletRequest request)
			throws NoteNotFoundException, LinkInformationException {

		String userId = request.getHeader("userId");
		List<NoteDTO> trashList = noteService.getTrash(userId);

		return new ResponseEntity<>(trashList, HttpStatus.OK);
	}

	/**
	 * To set color on the note
	 * 
	 * @param request
	 * @param noteId
	 * @param colour
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws NoteException
	 */
	@PutMapping(value = "/setColour/{noteId}")
	public ResponseEntity<Response> addColour(HttpServletRequest request, @PathVariable String noteId,
			@RequestParam String colour) throws NoteNotFoundException, UnauthorizedException, NoteException {

		String userId = request.getHeader("userId");
		noteService.addColour(userId, noteId, colour);

		Response response = new Response();
		response.setMessage("Colour add to the note");
		response.setStatus(20);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * to add reminder to the note
	 * 
	 * @param request
	 * @param noteId
	 * @param reminderDate
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws ReminderException
	 * @throws ParseException
	 */
	@PutMapping(value = "/addReminder/{noteId}")
	public ResponseEntity<Response> addReminder(HttpServletRequest request, @PathVariable String noteId,
			@RequestParam String reminder) throws NoteNotFoundException, UnauthorizedException, ReminderException {

		String userId = request.getHeader("userId");
		noteService.addNoteReminder(userId, noteId, reminder);

		Response response = new Response();
		response.setMessage("Reminder added to the note");
		response.setStatus(80);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * to remove reminder on the note
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/removeReminder/{noteId}")
	public ResponseEntity<Response> removeReminder(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.removeReminder(userId, noteId);

		Response response = new Response();
		response.setMessage("Removed reminder on the note");
		response.setStatus(81);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * To add pin to the note
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/addPin/{noteId}")
	public ResponseEntity<Response> addPin(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.addPin(userId, noteId);

		Response response = new Response();
		response.setMessage("Pinned the note");
		response.setStatus(70);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Remove pin on the note
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/removePin/{noteId}")
	public ResponseEntity<Response> removePin(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.removePin(userId, noteId);

		Response response = new Response();
		response.setMessage("Pin removed on the note");
		response.setStatus(71);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Add note to archive
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/addToArchive/{noteId}")
	public ResponseEntity<Response> addArchive(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {

		String userId = request.getHeader("userId");
		noteService.archiveNote(userId, noteId);

		Response response = new Response();
		response.setMessage("Archived the note");
		response.setStatus(60);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * remove note from archive
	 * 
	 * @param request
	 * @param noteId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@PutMapping(value = "/removeFromArchive/{noteId}")
	public ResponseEntity<Response> removeArchive(HttpServletRequest request, @PathVariable String noteId)
			throws NoteNotFoundException, UnauthorizedException {
		String userId = (String) request.getAttribute("UserId");

		noteService.removeArchiveNote(userId, noteId);

		Response response = new Response();
		response.setMessage("Remove archive note");
		response.setStatus(61);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * View archived notes
	 * 
	 * @param request
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 */
	@PostMapping(value = "/getArchiveNotes")
	public ResponseEntity<List<NoteDTO>> viewArchiveNotes(HttpServletRequest request)
			throws NoteNotFoundException, LinkInformationException {

		String userId = request.getHeader("userId");
		List<NoteDTO> archivedNoteList = noteService.getArchivedNote(userId);

		return new ResponseEntity<>(archivedNoteList, HttpStatus.OK);
	}

	/**
	 * Add label to the note
	 * 
	 * @param request
	 * @param noteId
	 * @param labelName
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws LabelException
	 * @throws LabelNotFoundException
	 * @throws InvalidLabelNameException
	 */
	@PutMapping(value = "/addLabelToNote/{noteId}")
	public ResponseEntity<Response> addLabel(HttpServletRequest request, @PathVariable String noteId,
			@RequestParam String labelName) throws NoteNotFoundException, UnauthorizedException, LabelException,
			LabelNotFoundException, InvalidLabelNameException {

		String userId = request.getHeader("userId");
		noteService.addLabel(userId, noteId, labelName);

		Response response = new Response();
		response.setMessage("Label added to the note");
		response.setStatus(201);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * To delete label from a note
	 * 
	 * @param request
	 * @param noteId
	 * @param labelId
	 * @return ResponseDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws LabelNotFoundException
	 */
	@PutMapping(value = "/deleteLabelFromNote/{noteId}")
	public ResponseEntity<Response> deteleLabelFromNote(HttpServletRequest request, @PathVariable String noteId,
			@RequestParam String labelId) throws NoteNotFoundException, UnauthorizedException, LabelNotFoundException {

		String userId = request.getHeader("userId");
		noteService.deleteNoteLabel(userId, noteId, labelId);

		Response response = new Response();
		response.setMessage("Label deleted from note");
		response.setStatus(203);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(value = "/sortNoteByTitleo(Or)Date")
	public ResponseEntity<List<NoteDTO>> sortNoteByTitle(HttpServletRequest request,@RequestParam(required=false) String type, @RequestParam(required= false ) String order)
			throws NoteNotFoundException {

		String userId = request.getHeader("userId");
		List<NoteDTO> listOfNotes=noteService.sortNote(userId, order,type);

		return new ResponseEntity<>(listOfNotes, HttpStatus.OK);
	}

}