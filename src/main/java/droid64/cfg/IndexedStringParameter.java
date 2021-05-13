package droid64.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IndexedStringParameter extends Parameter<List<String>> {

	private static final String REMOVE_INDEX = "\\.\\d+$";


	public IndexedStringParameter(String name) {
		super(name, ParameterType.INDEXED_STRING);
	}

    @Override
	public int compareTo(Parameter<?> other) {
    	return this.getName().replaceAll(REMOVE_INDEX, "").compareTo(other.getName().replaceAll(REMOVE_INDEX, ""));
    }

	@Override
	public void parse(String string) {
		if (string != null) {
			getValue().add(string);
		}
	}

	@Override
	public void setValue(List<String> newValue) {
		var list = new ArrayList<String>();
		super.setValue(list);
		if (newValue != null) {
			list.addAll(newValue);
		}
	}

	@Override
	public String toString() {
		return getName() + "=" + Optional.ofNullable(getValue()).map(m -> m.stream().collect(Collectors.joining(";"))).orElse("");
	}

}
