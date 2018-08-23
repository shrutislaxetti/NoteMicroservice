package com.bridgelabz.notemicroservice.model;

import java.util.List;

public class CreateNoteDTO {

	private String title;
	private String description;
	private String colour = "white";
	private String reminder;
	private boolean pin;
	private boolean archive;
	private List<String> listOfLabel;
	

	public CreateNoteDTO() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}

	public boolean getPin() {
		return pin;
	}

	public void setPin(boolean pin) {
		this.pin = pin;
	}

	public boolean getArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public List<String> getListOfLabel() {
		return listOfLabel;
	}

	public void setListOfLabel(List<String> listOfLabel) {
		this.listOfLabel = listOfLabel;
	}
}