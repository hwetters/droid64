package droid64.cfg;

public class IntegerParameter extends Parameter<Integer>{

	public IntegerParameter(String name) {
		super(name, ParameterType.INTEGER);
	}

	@Override
	public void parse(String string) {
		setValue(string != null ? Integer.valueOf(string.trim()) : null);
	}

	@Override
	public String toString() {
		Integer i = getValue();
		return getName() + "=" + (i != null ? Integer.toString(i) : "");
	}
}
