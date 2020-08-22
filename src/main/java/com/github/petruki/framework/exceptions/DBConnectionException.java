package com.github.petruki.framework.exceptions;

public class DBConnectionException extends Exception {
	
	private static final long serialVersionUID = 4875302664720336641L;

	public DBConnectionException(String reason, Throwable e) {
		super(reason, e);
	}

}
