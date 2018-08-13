package com.bridgelabz.notemicroservice.exceptions;

public class InvalidLabelNameException extends Exception {

	private static final long serialVersionUID = 849218790678650901L;

	public InvalidLabelNameException(String message) {
		super(message);
	}
}