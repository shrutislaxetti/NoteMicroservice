package com.bridgelabz.notemicroservice.exceptions;

public class UnauthorizedException extends Exception {

	private static final long serialVersionUID = 384989504318802790L;

	public UnauthorizedException(String message) {
		super(message);
	}
}