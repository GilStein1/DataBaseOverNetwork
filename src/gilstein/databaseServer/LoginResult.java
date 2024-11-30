package gilstein.databaseServer;

public enum LoginResult {

	SUCCESS(true),
	WRONG_PASSWORD(true),
	ERROR_UPON_LOGIN(true),
	SERVER_IS_OFFLINE(false);

	private final boolean loginSuccess;

	LoginResult(boolean loginSuccess) {
		this.loginSuccess = loginSuccess;
	}

	public boolean isLoginSuccessful() {
		return loginSuccess;
	}

}
