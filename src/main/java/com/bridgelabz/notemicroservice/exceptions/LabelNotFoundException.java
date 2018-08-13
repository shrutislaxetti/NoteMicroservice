package com.bridgelabz.notemicroservice.exceptions;

public class LabelNotFoundException extends Exception {

	private static final long serialVersionUID = 4787503821773648847L;

	public LabelNotFoundException(String message) {
		super(message);
	}
}