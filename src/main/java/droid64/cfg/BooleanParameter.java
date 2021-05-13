package droid64.cfg;

public class BooleanParameter extends Parameter<Boolean>{

	public BooleanParameter(String name) {
		super(name, ParameterType.BOOLEAN);
	}

	@Override
	public void parse(String string) {
		if (string == null) {
			setValue(false);
		} else {
			setValue("yes".equals(string.trim()) ? Boolean.TRUE : Boolean.valueOf(string.trim()));
		}
	}

	@Override
	public String toString() {
		return getName() + "=" + Boolean.TRUE.equals(getValue());
	}
}
