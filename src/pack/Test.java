package pack;

public class Test {

	private int a, b;
	private String c = "";

	public Test(int a, int b, String c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Test() {
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}
}
