package gilstein.util;

public enum LoginResult {

	SUCCESS(true),
	WRONG_PASSWORD(true),
	ERROR_UPON_LOGIN(true);

	private final boolean loginSuccess;

	LoginResult(boolean loginSuccess) {
		this.loginSuccess = loginSuccess;
	}

	public boolean isLoginSuccessful() {
		return loginSuccess;
	}

}
