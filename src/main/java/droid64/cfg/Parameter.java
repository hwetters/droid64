package droid64.cfg;

public abstract class Parameter<T> implements Comparable<Parameter<?>> {

	protected static final String STRING_LIST_SPLIT_EXPR = "\\s*[;]\\s*";
	protected static final String STRING_TUPEL_SPLIT_EXPR = "\\s*[,;.]\\s*";
	protected static final String DELIM = ";";
	protected static final String TUPEL_DELIM = ",";

	private final String name;
	private final ParameterType type;
	private T value;
	private T defaultValue;

	protected Parameter(String name, ParameterType type) {
		this.name = name;
		this.type = type;
	}

	public final String getName() {
		return name;
	}

	public final ParameterType getType() {
		return type;
	}

	public T getValue() {
		return value != null ? value : defaultValue;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public void reset() {
		setValue(defaultValue);
	}

	public abstract void parse(String string);


    @Override
	public int compareTo(Parameter<?> other) {
    	return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object that) {
    	return this == that || that instanceof Parameter && name.equals(((Parameter<?>)that).name);
    }

    @Override
    public int hashCode() {
    	return name.hashCode();
    }

}
