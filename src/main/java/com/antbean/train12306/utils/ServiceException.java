package com.antbean.train12306.utils;

public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = -1995421531997054168L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

}
