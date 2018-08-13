package com.bridgelabz.notemicroservice.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.notemicroservice.exceptions.GetLinkInfoException;
import com.bridgelabz.notemicroservice.exceptions.InvalidLabelNameException;
import com.bridgelabz.notemicroservice.exceptions.LabelException;
import com.bridgelabz.notemicroservice.exceptions.LabelNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.NoteNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.UnauthorizedException;
import com.bridgelabz.notemicroservice.model.LabelDTO;
import com.bridgelabz.notemicroservice.model.NoteDTO;
import com.bridgelabz.notemicroservice.model.Response;
import com.bridgelabz.notemicroservice.service.LabelService;

@RestController
@RequestMapping("/label")
public class LabelController {

	@Autowired
	private LabelService labelService;

	/**
	 * to create a new Label
	 * 
	 * @param request
	 * @param labelName
	 * @return ResponseDTO
	 * @throws LabelException
	 * @throws InvalidLabelNameException
	 */
	@PostMapping(value = "/createLabel")
	public ResponseEntity<LabelDTO> createLabel(HttpServletRequest request, @RequestParam String labelName)
			throws LabelException, InvalidLabelNameException {
		
		String userId = request.getHeader("userId");
		LabelDTO labelDto = labelService.createLabel(userId, labelName);

		return new ResponseEntity<>(labelDto, HttpStatus.OK);
	}

	/***
	 * To get all labels
	 * 
	 * @param request
	 * @param labelId
	 * @return list of LabelDTO
	 * @throws LabelNotFoundException
	 */
	@PostMapping(value = "/getLabels")
	public ResponseEntity<List<LabelDTO>> getAllLabel(HttpServletRequest request) throws LabelNotFoundException {

		String userId = request.getHeader("userId");
		List<LabelDTO> labelList = labelService.getAllLabel(userId);

		return new ResponseEntity<>(labelList, HttpStatus.OK);
	}

	/**
	 * to update label name
	 * 
	 * @param request
	 * @param labelId
	 * @param labelName
	 * @return ResponseDTO
	 * @throws UnauthorizedException
	 * @throws LabelNotFoundException
	 */
	@PutMapping(value = "/updateLabel/{labelId}")
	public ResponseEntity<Response> editLabel(HttpServletRequest request, @PathVariable String labelId,
			@RequestParam String labelName) throws UnauthorizedException, LabelNotFoundException {

		String userId = request.getHeader("userId");
		labelService.updateLabel(userId, labelId, labelName);

		Response response = new Response();
		response.setMessage("Label edited");
		response.setStatus(202);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * To delete label
	 * 
	 * @param request
	 * @param labelId
	 * @return ResponseDTO
	 * @throws UnauthorizedException
	 * @throws LabelNotFoundException
	 */
	@DeleteMapping(value = "/deleteLabel/{labelId}")
	public ResponseEntity<Response> deleteLabel(HttpServletRequest request, @PathVariable String labelId)
			throws LabelNotFoundException {

		String userId = request.getHeader("userId");
		labelService.deleteLabel(userId, labelId);

		Response response = new Response();
		response.setMessage("Label deleted");
		response.setStatus(202);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * To get all notes by label Id
	 * 
	 * @param request
	 * @param labelId
	 * @return List of NoteDTO
	 * @throws LabelNotFoundException
	 * @throws NoteNotFoundException
	 * @throws GetLinkInfoException
	 */
	@PostMapping(value = "/getLabel{labelId}")
	public ResponseEntity<List<NoteDTO>> getLabel(HttpServletRequest request, @PathVariable String labelId)
			throws LabelNotFoundException, GetLinkInfoException, NoteNotFoundException {

		String userId = request.getHeader("userId");
		List<NoteDTO> notes = labelService.getLabel(userId, labelId);

		return new ResponseEntity<>(notes, HttpStatus.OK);
	}
}