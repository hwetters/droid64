package droid64.cfg;

import java.io.File;
import java.util.Optional;

public class FileParameter extends Parameter<File> {

	public FileParameter(String name) {
		super(name, ParameterType.FILE);
	}

	@Override
	public void parse(String string) {
		setValue(string != null ? new File(string) : null);
	}

	@Override
	public String toString() {
		return getName() + "=" + Optional.ofNullable(getValue()).map(File::toString).orElse("");
	}

}
