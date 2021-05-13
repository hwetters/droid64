package droid64.cfg;

public class StringParameter extends Parameter<String>{


	public StringParameter(String name) {
		super(name, ParameterType.STRING);
	}

	@Override
	public void parse(String string) {
		setValue(string);
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}
}
