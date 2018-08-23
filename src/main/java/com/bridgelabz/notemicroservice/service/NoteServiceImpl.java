package com.bridgelabz.notemicroservice.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bridgelabz.notemicroservice.exceptions.LinkInformationException;
import com.bridgelabz.notemicroservice.exceptions.InvalidLabelNameException;
import com.bridgelabz.notemicroservice.exceptions.LabelException;
import com.bridgelabz.notemicroservice.exceptions.LabelNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.NoteException;
import com.bridgelabz.notemicroservice.exceptions.NoteNotFoundException;
import com.bridgelabz.notemicroservice.exceptions.ReminderException;
import com.bridgelabz.notemicroservice.exceptions.UnauthorizedException;
import com.bridgelabz.notemicroservice.model.CreateNoteDTO;
import com.bridgelabz.notemicroservice.model.Label;
import com.bridgelabz.notemicroservice.model.LabelDTO;
import com.bridgelabz.notemicroservice.model.Note;
import com.bridgelabz.notemicroservice.model.NoteDTO;
import com.bridgelabz.notemicroservice.model.URLMetaData;
import com.bridgelabz.notemicroservice.model.UpdateNote;
import com.bridgelabz.notemicroservice.repository.LabelElasticsearchRepository;
import com.bridgelabz.notemicroservice.repository.LabelRepository;
import com.bridgelabz.notemicroservice.repository.NoteElasticsearchRepository;
import com.bridgelabz.notemicroservice.repository.NoteRepository;
import com.bridgelabz.notemicroservice.util.LinkInfoProvider;
import com.bridgelabz.notemicroservice.util.NoteUtility;

@Service
public class NoteServiceImpl implements NoteService {

	@Autowired
	private NoteRepository noteRepository;

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private NoteElasticsearchRepository noteElasticsearchRepository;

	@Autowired
	private LabelElasticsearchRepository labelElasticsearchRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private Environment environment;

	@Autowired
	private LinkInfoProvider linkinfo;

	/**
	 * create a new note
	 * 
	 * @param createNoteDto
	 * @param jwToken
	 * @return ViewNoteDTO
	 * @throws NoteException
	 * @throws ReminderException
	 * @throws ParseException
	 * @throws LinkInformationException
	 */
	@Override
	public NoteDTO createNote(CreateNoteDTO createNoteDto, String userId) throws NoteException, ReminderException {

		NoteUtility.validateNewNote(createNoteDto);
		Note note = new Note();
		note.setTitle(createNoteDto.getTitle());
		note.setDescription(createNoteDto.getDescription());
		if (createNoteDto.getColour() != null || createNoteDto.getColour().trim().length() != 0) {
			note.setColour(createNoteDto.getColour());
		}
		if (createNoteDto.getReminder() != null || createNoteDto.getReminder().trim().length() != 0) {
			NoteUtility.validateDate(createNoteDto.getReminder());
			note.setReminder(createNoteDto.getReminder());
		}
		note.setUserId(userId);
		note.setCreatedAt(NoteUtility.getCurrentDate());
		note.setLastUpdated(NoteUtility.getCurrentDate());
		note.setArchive(createNoteDto.getArchive());
		note.setPin(createNoteDto.getPin());

		List<String> labelNameList = createNoteDto.getListOfLabel();

		List<Label> userLabels = labelRepository.findAllByUserId(userId);

		ArrayList<String> userLabelList = new ArrayList<>();
		for (int j = 0; j < userLabels.size(); j++) {
			userLabelList.add(userLabels.get(j).getLabelName());
		}
		ArrayList<LabelDTO> labels = new ArrayList<>();

		for (int i = 0; i < labelNameList.size(); i++) {
			if (labelNameList.get(i) != null || labelNameList.get(i).trim().equals("")) {

				if (!userLabelList.contains(labelNameList.get(i))) {
					Label label = createLabel(labelNameList.get(i), userId);
					LabelDTO newlabel = new LabelDTO();

					labelRepository.save(label);
					labelElasticsearchRepository.save(label);
					labels.add(newlabel);

				} else {
					List<Label> optionalLabelToSave = labelElasticsearchRepository
							.findAllByLabelName(labelNameList.get(i));

					for (int j = 0; j < optionalLabelToSave.size(); j++) {
						if (optionalLabelToSave.get(j).getUserId().equalsIgnoreCase(userId)) {
							LabelDTO viewLabel = new LabelDTO();
							viewLabel.setLabelName(optionalLabelToSave.get(j).getLabelName());
							viewLabel.setLabelId(optionalLabelToSave.get(j).getLabelId());
							viewLabel.setCreatedAt(optionalLabelToSave.get(j).getCreatedAt());
							labels.add(viewLabel);
						}
					}
				}
				note.setListOfLabel(labels);
			}
			try {
				note.setUrlList(getListOfUrl(createNoteDto.getDescription()));
			} catch (LinkInformationException e) {
				e.printStackTrace();
			}
			noteRepository.save(note);
			noteElasticsearchRepository.save(note);
		}
		return modelMapper.map(note, NoteDTO.class);
	}

	public Label createLabel(String labelName, String userId) {

		Label label = new Label();
		label.setLabelName(labelName);
		label.setUserId(userId);
		label.setCreatedAt(new Date());
		return label;

	}

	public List<URLMetaData> getListOfUrl(String description) throws LinkInformationException {
		String[] strArray = description.split(" ");
		List<URLMetaData> list = new ArrayList<>();
		for (int k = 0; k < strArray.length; k++) {
			if (NoteUtility.validateUrl(strArray[k])) {

				list.add(linkinfo.getLinkInformation(strArray[k]));
			}
		}
		return list;
	}

	/**
	 * view a note
	 * 
	 * @param userId
	 * @param noteId
	 * @return ViewNoteDTO
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws LinkInformationException
	 */
	public NoteDTO viewNote(String userId, String noteId)
			throws NoteNotFoundException, UnauthorizedException, LinkInformationException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();

		return modelMapper.map(note, NoteDTO.class);

	}

	/**
	 * view a list of note owned by the user
	 * 
	 * @param userId
	 * @return list of ViewNoteDTO
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 */
	public List<NoteDTO> viewAllNotes(String userId) throws NoteNotFoundException, LinkInformationException {

		List<Note> noteList = noteElasticsearchRepository.findAllByUserIdAndTrash(userId, false);

		if (noteList.isEmpty()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		List<NoteDTO> noteDtos = noteList.stream().map(filterNote -> modelMapper.map(filterNote, NoteDTO.class))
				.collect(Collectors.toList());

		List<NoteDTO> pinnedNoteDtoList = noteDtos.stream().filter(NoteDTO::getPin).collect(Collectors.toList());

		List<NoteDTO> unpinnedNoteDtoList = noteDtos.stream().filter(noteStream -> !noteStream.getPin())
				.collect(Collectors.toList());
		return Stream.concat(pinnedNoteDtoList.stream(), unpinnedNoteDtoList.stream()).collect(Collectors.toList());

	}

	/**
	 * update a note
	 * 
	 * @param updateNoteDTO
	 * @param userId
	 * @param noteId
	 * @throws NoteException
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws ReminderException
	 * @throws ParseException
	 */
	@Override
	public void updateNote(UpdateNote updateNote, String userId, String noteId)
			throws NoteException, NoteNotFoundException, UnauthorizedException, ReminderException {

		NoteUtility.validateUpdateNoteDetails(updateNote);

		Optional<Note> optionalNote = noteRepository.findByNoteIdAndUserId(noteId, userId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();

		note.setTitle(updateNote.getTitle());
		note.setLastUpdated(NoteUtility.getCurrentDate());
		note.setDescription(updateNote.getDescription());
		note.setLastUpdated(NoteUtility.getCurrentDate());
		if (NoteUtility.validateDate(updateNote.getReminder())) {
			note.setReminder(updateNote.getReminder());
		}
		try {
			note.setUrlList(getListOfUrl(updateNote.getDescription()));
		} catch (LinkInformationException e) {
			e.printStackTrace();
		}
		note.setLastUpdated(NoteUtility.getCurrentDate());
		note.setColour(updateNote.getColour());

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * move a note to trash
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@Override
	public void deleteNote(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setTrash(true);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * delete a note from trash
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@Override
	public void permanentNoteDelete(String userId, String noteId, boolean delete)
			throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		if (!optionalNote.get().getTrash()) {
			throw new NoteNotFoundException("No such note found in trash");
		}

		if (delete) {
			noteRepository.deleteById(noteId);

			noteElasticsearchRepository.deleteById(noteId);
		} else {
			Note note = optionalNote.get();
			note.setTrash(false);
			noteRepository.save(note);

			noteElasticsearchRepository.save(note);
		}

	}

	/**
	 * delete all notes in trash
	 * 
	 * @param userId
	 * @throws NoteNotFoundException
	 */
	@Override
	public void emptyTrash(String userId) throws NoteNotFoundException {

		List<Note> noteList = noteRepository.findAllByUserId(userId);

		if (noteList.isEmpty()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		for (int i = 0; i < noteList.size(); i++) {
			Note note = noteList.get(i);
			if (noteList.get(i).getTrash()) {
				noteRepository.deleteById(note.getNoteId());

				noteElasticsearchRepository.deleteById(note.getNoteId());
			}
		}
	}

	/**
	 * To view all trashed notes
	 * 
	 * @param userId
	 * @return list of notes
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 * 
	 */
	@Override
	public List<NoteDTO> getTrash(String userId) throws NoteNotFoundException, LinkInformationException {

		List<Note> noteList = noteElasticsearchRepository.findAllByUserIdAndTrash(userId, true);

		if (noteList.isEmpty()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		return noteList.stream().map(filterNote -> modelMapper.map(filterNote, NoteDTO.class))
				.collect(Collectors.toList());

	}

	/**
	 * To set color on the note
	 * 
	 * @param userId
	 * @param noteId
	 * @param colour
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws NoteException
	 */
	@Override
	public void addColour(String userId, String noteId, String colour)
			throws NoteNotFoundException, UnauthorizedException, NoteException {

		if (colour == null || colour.trim().length() == 0) {
			throw new NoteException("Color cannot be empty");
		}

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setColour(colour);
		note.setLastUpdated(NoteUtility.getCurrentDate());

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * to add reminder to the note
	 * 
	 * @param userId
	 * @param noteId
	 * @param reminderDate
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws ReminderException
	 * @throws ParseException
	 */
	@Override
	public void addNoteReminder(String userId, String noteId, String reminderDate)
			throws NoteNotFoundException, UnauthorizedException, ReminderException {

		NoteUtility.validateDate(reminderDate);

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setReminder(reminderDate);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * to remove reminder from the note
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@Override
	public void removeReminder(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setReminder(null);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * Add pin to the note
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * 
	 */
	@Override
	public void addPin(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();

		if (!note.getPin()) {
			note.setArchive(false);
			note.setPin(true);
		} else {
			note.setPin(false);
		}

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * Removes pin on the note
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * 
	 */
	@Override
	public void removePin(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setPin(false);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * Adding note to archive
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * 
	 */
	@Override
	public void archiveNote(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setPin(false);
		note.setArchive(true);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * remove note from archive
	 * 
	 * @param userId
	 * @param noteId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 */
	@Override
	public void removeArchiveNote(String userId, String noteId) throws NoteNotFoundException, UnauthorizedException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();
		note.setArchive(false);

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * View archived notes
	 * 
	 * @param userId
	 * @return list of archived notes
	 * @throws NoteNotFoundException
	 * @throws LinkInformationException
	 * 
	 */
	@Override
	public List<NoteDTO> getArchivedNote(String userId) throws NoteNotFoundException, LinkInformationException {

		List<Note> noteList = noteElasticsearchRepository.findAllByUserIdAndTrash(userId, false);

		if (noteList.isEmpty()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		List<NoteDTO> noteDtos = noteList.stream().map(filterNote -> modelMapper.map(filterNote, NoteDTO.class))
				.collect(Collectors.toList());

		return noteDtos.stream().filter(NoteDTO::getArchive).collect(Collectors.toList());

	}

	/**
	 * add label to the note
	 * 
	 * @param userId
	 * @param noteId
	 * @return list of note
	 * @throws NoteNotFoundException
	 * @throws LabelNotFoundException
	 * @throws UnauthorizedException
	 * @throws LabelException
	 * @throws InvalidLabelNameException
	 */
	@Override
	public void addLabel(String userId, String noteId, String labelName) throws NoteNotFoundException,
			UnauthorizedException, LabelException, LabelNotFoundException, InvalidLabelNameException {

		if (labelName == null || labelName.trim().length() == 0) {
			throw new InvalidLabelNameException("Invalid LabelName");
		}

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Note note = optionalNote.get();

		Optional<Label> optionalLabel = labelRepository.findByLabelNameAndUserId(labelName, userId);

		if (optionalLabel.isPresent()) {
			Label label = optionalLabel.get();
			LabelDTO labelDto = modelMapper.map(label, LabelDTO.class);
			List<LabelDTO> labelDtoList = Stream.concat(note.getListOfLabel().stream(), Stream.of(labelDto))
					.collect(Collectors.toList());
			note.setListOfLabel(labelDtoList);
		} else {
			Label label = new Label();
			label.setLabelName(labelName);
			label.setUserId(userId);
			labelRepository.save(label);

			labelElasticsearchRepository.save(label);

			LabelDTO labelDto = modelMapper.map(label, LabelDTO.class);
			List<LabelDTO> labelDtoList = Stream.concat(note.getListOfLabel().stream(), Stream.of(labelDto))
					.collect(Collectors.toList());
			note.setListOfLabel(labelDtoList);
		}

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	/**
	 * to delete a label from the note
	 * 
	 * @param userId
	 * @param noteId
	 * @param labelId
	 * @throws NoteNotFoundException
	 * @throws UnauthorizedException
	 * @throws LabelNotFoundException
	 */
	@Override
	public void deleteNoteLabel(String userId, String noteId, String labelId)
			throws NoteNotFoundException, UnauthorizedException, LabelNotFoundException {

		Optional<Note> optionalNote = noteRepository.findById(noteId);

		if (!optionalNote.isPresent()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}
		if (!optionalNote.get().getUserId().equals(userId)) {
			throw new UnauthorizedException(environment.getProperty("UnauthorizedUser"));
		}

		Optional<Label> optionalLabel = labelRepository.findById(labelId);

		if (!optionalLabel.isPresent()) {
			throw new LabelNotFoundException("No such label found");
		}

		Note note = optionalNote.get();

		if (!note.getTrash()) {
			List<LabelDTO> labelList = note.getListOfLabel();
			for (int i = 0; i < labelList.size(); i++) {
				if (labelList.get(i).getLabelId().equals(labelId)) {
					labelList.remove(i);
				}
			}
			note.setListOfLabel(labelList);
		}

		noteRepository.save(note);

		noteElasticsearchRepository.save(note);
	}

	@Override
	public List<NoteDTO> sortNote(String userId, String order, String type) throws NoteNotFoundException {

		List<Note> noteList = noteElasticsearchRepository.findAllByUserId(userId);
		List<NoteDTO> noteDtos = new ArrayList<>();
		if (noteList.isEmpty()) {
			throw new NoteNotFoundException(environment.getProperty("NoteNotFound"));
		}

		
		if (type == null) {
			noteDtos = noteList.stream().sorted(Comparator.comparing(Note::getTitle))
					.map(sortedNote -> modelMapper.map(sortedNote, NoteDTO.class)).collect(Collectors.toList());
		} else if (type.equalsIgnoreCase("title")) {
			if (order == null || order.equalsIgnoreCase("desc")) {

				return noteList.stream().sorted(Comparator.comparing(Note::getTitle).reversed())
						.map(sortedNote -> modelMapper.map(sortedNote, NoteDTO.class)).collect(Collectors.toList());
			}

			noteDtos = noteList.stream().sorted(Comparator.comparing(Note::getTitle))
					.map(sortedNote -> modelMapper.map(sortedNote, NoteDTO.class)).collect(Collectors.toList());

		} else if (type.equalsIgnoreCase("date")) {
			if (order == null || order.equalsIgnoreCase("desc")) {
				return noteList.stream().sorted(Comparator.comparing(Note::getCreatedAt))
						.map(sortedNote -> modelMapper.map(sortedNote, NoteDTO.class)).collect(Collectors.toList());
			}

			noteDtos = noteList.stream().sorted(Comparator.comparing(Note::getCreatedAt).reversed())
					.map(sortedNote -> modelMapper.map(sortedNote, NoteDTO.class)).collect(Collectors.toList());

		}

		return noteDtos;

	}
}
