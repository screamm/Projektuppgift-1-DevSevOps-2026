package se.devsecops.backend.model;

public class CreateUserResponse {
	private boolean exists;
	private String message;

	public CreateUserResponse() {
	}

	public boolean isExists() {
		return exists;
	}
	
	public CreateUserResponse(boolean exists, String message) {
	    this.exists = exists;
	    this.message = message;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}