package droid64.cfg;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class FontParameter extends Parameter<Font>{
	private static final float DEFAULT_SIZE = 12;

	public FontParameter(String name) {
		super(name, ParameterType.FONT);
	}

	@Override
	public void parse(String string) {
		setValue(parseFont(string));
	}

	@Override
	public String toString() {
		return getName() + "=" + Optional.ofNullable(getValue()).map(a->a.getName() + DELIM + a.getStyle() + DELIM + a.getSize()).orElse("");
	}

	private Font parseFont(String string) {
		if (string != null && !string.isEmpty()) {
			var sa = string.split(STRING_LIST_SPLIT_EXPR);
			if (sa.length >= 3) {
				return new Font(sa[0].trim(), Integer.parseInt(sa[1].trim()), Integer.parseInt(sa[2].trim()));
			} else if (new File(string).isFile()) {
				try (FileInputStream input = new FileInputStream(string)) {
					return Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(DEFAULT_SIZE);
				} catch (FontFormatException | IOException e) {
					throw new IllegalArgumentException("Failed to load font " + string + ".");
				}
			} else {
				throw new IllegalArgumentException(getName() + " is missing attributes. ");
			}
		}
		return null;
	}
}