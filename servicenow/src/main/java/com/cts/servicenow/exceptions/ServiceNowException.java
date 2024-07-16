package com.cts.servicenow.exceptions;

public class ServiceNowException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceNowException(String message, Throwable cause) {
        super(message, cause);
    }
}
