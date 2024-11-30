package gilstein.util;

public enum Result {

	NOT_A_VALID_KEY("notAValidKey"),
	ERROR("error"),
	SUCCESS("success");

	private String message;

	Result(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
