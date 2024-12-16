package gilstein.util;

public class User {

	private String userName;
	private String password;
	private int id;

	public User(String userName, String password, int id) {
		this.userName = userName;
		this.password = password;
		this.id = id;
	}

	public User(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public User() {
	}

	public String userName() {
		return userName;
	}

	public String password() {
		return password;
	}

	public int id() {
		return id;
	}

}
