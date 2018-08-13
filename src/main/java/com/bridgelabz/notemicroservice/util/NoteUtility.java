package com.bridgelabz.notemicroservice.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bridgelabz.notemicroservice.exceptions.NoteException;
import com.bridgelabz.notemicroservice.exceptions.ReminderException;
import com.bridgelabz.notemicroservice.model.CreateNote;
import com.bridgelabz.notemicroservice.model.UpdateNote;


public class NoteUtility {
	
	private NoteUtility() {

	}

	private static final Pattern URL_REGEX = Pattern.compile("(http(s)?://)?([\\w-]+\\.)+[\\w-]+(/[\\w- ;,./?%&=]*)?");

	/**
	 * validation for the new note creation
	 * 
	 * @param note
	 * @return boolean
	 * @throws NoteException
	 */
	public static void validateNewNote(CreateNote note) throws NoteException {
		if ((note.getTitle() == null || note.getTitle().trim().length() == 0) && (note.getDescription() == null)
				|| note.getDescription().trim().length() == 0
						&& (note.getColour() == null || note.getColour().trim().length() == 0)) {
			throw new NoteException("Title ,Description and Colour cannot be empty");
		}
	}

	/**
	 * to get the current system date;
	 * 
	 * @return Date
	 */
	public static Date getCurrentDate() {
		Date date = new Date();
		return date;
	}

	/**
	 * to validate date
	 * 
	 * @param date
	 * @return true if validate successful else false
	 * @throws ReminderException
	 * @throws ParseException
	 */
	public static boolean validateDate(String date) throws ReminderException {
		Date reminder = null;
		try {
			reminder = new SimpleDateFormat("dd/MM/yyyy").parse(date);
		} catch (ParseException exception) {
			throw new ReminderException("Error while parsing reminder");
		}

		if (reminder.before(getCurrentDate())) {
			throw new ReminderException("Date and time should be current date and time or after");
		}
		return true;
	}

	/**
	 * To validate url
	 * 
	 * @param url
	 * @return
	 */
	public static boolean validateUrl(String url) {
		Matcher matcher = URL_REGEX.matcher(url);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	/**
	 * To get the url list from string list
	 * 
	 * @param stringArray
	 * @return
	 */
	public static List<String> getUrlList(String[] stringArray) {

		List<String> urlList = new ArrayList<>();

		for (int i = 0; i < stringArray.length; i++) {
	
			if (validateUrl(stringArray[i])) {
				urlList.add(stringArray[i]);
			}
		}
		return urlList;
	}

	public static void validateUpdateNoteDetails(UpdateNote updateNote) throws NoteException, ReminderException {
		if (updateNote.getTitle() == null || updateNote.getTitle().trim().length() == 0) {
			throw new NoteException("Invalid details!!");
		}
		if (updateNote.getDescription() == null || updateNote.getDescription().trim().length() == 0) {
			throw new NoteException("Invalid details!!");
		}
		
		if (updateNote.getColour() == null || updateNote.getColour().trim().length() == 0) {
			throw new NoteException("Invalid details!!");
		}
	}

}