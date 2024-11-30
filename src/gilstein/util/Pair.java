package gilstein.util;

public class Pair<R, C> {

	private R first;
	private C second;

	public Pair(R first, C second) {
		this.first = first;
		this.second = second;
	}

	public R getFirst() {
		return first;
	}

	public void setFirst(R first) {
		this.first = first;
	}

	public C getSecond() {
		return second;
	}

	public void setSecond(C second) {
		this.second = second;
	}

}
