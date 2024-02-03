package droid64.cfg;

import java.awt.Color;
import java.util.Optional;

public class ColorParameter extends Parameter<Color>{

	/** Constructor
	 *
	 * @param name
	 */
	public ColorParameter(String name) {
		super(name, ParameterType.COLOR);
	}

	@Override
	public void parse(String string) {
		setValue(parseColor(string));
	}

	public void setValue(Integer value) {
		setValue(new Color(value));
	}

	private Color parseColor(String str) {
		if (str == null) {
			return null;
		}
		var split = str.trim().split(STRING_TUPEL_SPLIT_EXPR);
		if (split.length >= 3) {
			int r = Integer.parseInt(split[0].trim()) & 0xff;
			int g = Integer.parseInt(split[1].trim()) & 0xff;
			int b = Integer.parseInt(split[2].trim()) & 0xff;
			return new Color(r, g, b);
		}
		return null;
	}

	@Override
	public String toString() {
		return getName() + "=" + Optional.ofNullable(getValue())
		.map( c -> c.getRed() + TUPEL_DELIM + c.getGreen() + TUPEL_DELIM + c.getBlue())
		.orElse("");
	}

}
