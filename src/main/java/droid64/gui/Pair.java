package droid64.gui;

public class Pair {

	private final int num;
	private final String name;

	public Pair(int num, String name) {
		this.num = num;
		this.name = name;
	}

	public int getNum() {
		return num;
	}

	@Override
	public String toString() {
		return name;
	}

}
