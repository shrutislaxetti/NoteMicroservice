package com.bridgelabz.notemicroservice.exceptionhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bridgelabz.notemicroservice.exceptions.LinkInformationException;
import com.bridgelabz.notemicroservice.exceptions.InvalidLabelNameException;
import com.bridgelabz.notemicroservice.exceptions.LabelException;
import com.bridgelabz.notemicroservice.exceptions.LabelNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.NoteException;
import com.bridgelabz.notemicroservice.exceptions.NoteNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.ReminderException;
import com.bridgelabz.notemicroservice.exceptions.UnauthorizedException;
import com.bridgelabz.notemicroservice.model.Response;

public class NoteExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(NoteExceptionHandler.class);

	@ExceptionHandler(NoteException.class)
	public ResponseEntity<Response> handleRegistrationException(NoteException exception) {
		logger.info("Error occured while creating new node " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(91);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoteNotFoundException.class)
	public ResponseEntity<Response> handleNoteNotFoundException(NoteNotFoundException exception) {
		logger.info("Error while searching for noteId " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(92);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ReminderException.class)
	public ResponseEntity<Response> handleReminderException(ReminderException exception) {
		logger.info("Error while setting reminder " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(4);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Response> handleUnauthorizedException(UnauthorizedException exception) {
		logger.info("Error while authentication " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(93);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(LabelException.class)
	public ResponseEntity<Response> handleLabelException(LabelException exception) {
		logger.info("Error while operating with label " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(94);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(LabelNotFoundException.class)
	public ResponseEntity<Response> handleLabelNotFoundException(LabelNotFoundException exception) {
		logger.info("Error while searching label " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(95);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InvalidLabelNameException.class)
	public ResponseEntity<Response> handleInvalidLabelNameException(InvalidLabelNameException exception) {
		logger.info("Error while validating label name " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(96);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(LinkInformationException.class)
	public ResponseEntity<Response> handleGetLinkInfoException(LinkInformationException exception) {
		logger.info("Error while getting link information " + exception.getMessage(), exception);

		Response response = new Response();
		response.setMessage(exception.getMessage());
		response.setStatus(97);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

}