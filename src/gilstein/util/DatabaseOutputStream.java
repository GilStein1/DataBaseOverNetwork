package gilstein.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DatabaseOutputStream extends DataOutputStream {

	public DatabaseOutputStream(OutputStream out) {
		super(out);
	}

	public void write(String value) throws IOException {
		write((value + "\n").getBytes());
	}

	public void sendResultMessage(Result result) throws IOException {
		write(result.getMessage());
	}

}
