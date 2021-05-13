package droid64.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import droid64.d64.Utility;

public class ListParameter extends Parameter<List<String>> {

	public ListParameter(String name) {
		super(name, ParameterType.STRING_LIST);
	}

	@Override
	public void parse(String string) {

		if (Utility.isEmpty(string)) {
			reset();
		} else {
			var list = new ArrayList<String>();
			Collections.addAll(list, string.replaceAll("[\\r\\n]+$", "").split(STRING_LIST_SPLIT_EXPR));
			super.setValue(new ArrayList<>(list));
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
		var list = getValue();
		String sl = Optional.ofNullable(list).map(List::stream).orElseGet(Stream::empty).collect(Collectors.joining(DELIM));
		return  getName() + "="  + (sl.isEmpty() ? "" : sl);

	}
}
