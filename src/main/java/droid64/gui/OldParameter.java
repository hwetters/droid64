package droid64.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import droid64.cfg.ParameterType;
import droid64.d64.Utility;

/**<pre style='font-family:sans-serif;'>
 * Created on 03.05.2018
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 *
 * @author henrik
 * </pre>
 */
public class OldParameter {

	private static final String DELIM = ";";
	private static final String LF = "\n";
	private static final String EQ = "=";
	private static final String DOT = ".";
	private static final String STRING_LIST_SPLIT_EXPR = "\\s*[;]\\s*";

	private final String name;
	private final ParameterType type;
	private Object value;

	public OldParameter(String name, ParameterType type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		OldParameter that = (OldParameter) obj;
		if (this.type != that.getType()) {
			return false;
		}
		if (!stringsEqual(name, that.name)) {
			return false;
		}
		if (this.value == null || that.value == null) {
			return this.value == null && that.value == null;
		}
		return this.value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return (value != null ? value.hashCode() : 0) + (name != null ? name.hashCode() : 0) + type.hashCode();
	}

	private boolean stringsEqual(String s1, String s2) {
		return s1 != null ? s1.equals(s2) : s2 == null;
	}

	public void parseValue(String lineValue) {
		try {
			switch (type) {
			case STRING:
				setStringValue(lineValue != null ? lineValue.replaceAll("[\\r\\n]+$", Utility.EMPTY) : null);
				break;
			case INTEGER:
				setIntegerValue(lineValue != null ? Integer.valueOf(lineValue.trim()) : null);
				break;
			case BOOLEAN:
				if (lineValue == null) {
					setBooleanValue(false);
				} else {
					setBooleanValue("yes".equals(lineValue.trim()) ? Boolean.TRUE : Boolean.valueOf(lineValue.trim()));
				}
				break;
			case COLOR:
				splitStringIntoColorParam(lineValue);
				break;
			case FONT:
				setFontValue(lineValue);
				break;
			case STRING_LIST:
				setStringListParam(lineValue);
				break;
			default:
			}
		} catch (IllegalArgumentException e) {	//NOSONAR
			System.err.println("Failed to parse setting: "+e.getMessage());	//NOSONAR
		}
	}

	public void setStringListParam(String value) {
		List<String> list = new ArrayList<>();
		setStringListValue(list);
		if (value != null) {
			for (String str : Arrays.asList(value.replaceAll("[\\r\\n]+$", Utility.EMPTY).split(STRING_LIST_SPLIT_EXPR))) {
				list.add(str);
			}
		}
	}

	private void splitStringIntoColorParam(String str) {
		if (str == null) {
			return;
		}
		String[] split = str.trim().split("\\s*[,;.]\\s*");
		if (split.length >= 3) {
			int r = Integer.parseInt(split[0].trim()) & 0xff;
			int g = Integer.parseInt(split[1].trim()) & 0xff;
			int b = Integer.parseInt(split[2].trim()) & 0xff;
			setColorValue(new Color(r, g, b));
		}
	}

	public ParameterType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	/** Avoid using. Use get&lt;<i>Type</i>&gt;value() instead.
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Parameter[");
		buf.append(" .name=").append(name);
		buf.append(" .type=").append(type);
		buf.append(" .value=").append(value);
		buf.append(']');
		return buf.toString();
	}

	// Getters

	public Boolean getBooleanValue() {
		if (type == ParameterType.BOOLEAN && (value instanceof Boolean || value == null)) {
			return (Boolean) value;
		}
		throw new IllegalArgumentException(name + " is not a boolean parameter.");
	}

	public Color getColorValue() {
		if (type == ParameterType.COLOR && (value instanceof Color || value == null)) {
			return (Color) value;
		}
		throw new IllegalArgumentException(name + " is not a color parameter.");
	}

	public Font getFontValue() {
		if (type == ParameterType.FONT && (value instanceof Font || value == null)) {
			return (Font) value;
		}
		throw new IllegalArgumentException(name + " is not a font parameter.");
	}

	@SuppressWarnings("unchecked")
	public List<String> getIndexedStringValue() {
		if (type == ParameterType.INDEXED_STRING) {
			if (value == null) {
				value = new ArrayList<>();
			}
			if (value instanceof List) {
				return (List<String>) value;
			}
		}
		throw new IllegalArgumentException(name + " is not an indexed string parameter.");
	}

	public Integer getIntegerValue() {
		if (type == ParameterType.INTEGER && (value instanceof Integer || value == null)) {
			return (Integer) value;
		}
		throw new IllegalArgumentException(name + " is not an integer parameter.");
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringListValue() {
		if (type == ParameterType.STRING_LIST) {
			if (value == null) {
				value = new ArrayList<String>();
			}
			if (value instanceof List) {
				return (List<String>) value;
			}
		}
		throw new IllegalArgumentException(name + " is not a list parameter. ");
	}

	public String getStringValue() {
		if (type == ParameterType.STRING && (value instanceof String || value == null)) {
			return (String) value;
		}
		throw new IllegalArgumentException(name + " is not string parameter.");
	}

	// Setters

	public void setBooleanValue(Boolean value) {
		if (type == ParameterType.BOOLEAN) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a boolean parameter.");
		}
	}

	public void setColorValue(Color value) {
		if (type == ParameterType.COLOR) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a color parameter.");
		}
	}

	public void setFontValue(Font value) {
		if (type == ParameterType.FONT) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a font parameter.");
		}
	}

	public void setFontValue(String str) {
		if (str != null && !str.isEmpty()) {
			String[] sa = str.split(STRING_LIST_SPLIT_EXPR);
			if (sa.length >= 3) {
				setFontValue(new Font(sa[0].trim(), Integer.parseInt(sa[1].trim()), Integer.parseInt(sa[2].trim())));
			} else {
				throw new IllegalArgumentException(name + " is missing attributes. ");
			}
		}
	}

	public void setIndexedStringValue(List<String> list) {
		if (type == ParameterType.INDEXED_STRING) {
			this.value = list;
		} else {
			throw new IllegalArgumentException(name + " is not an indexed list parameter. ");
		}
	}

	public void setIntegerValue(Integer value) {
		if (type == ParameterType.INTEGER) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not an integer parameter.");
		}
	}

	public void setStringListValue(List<String> list) {
		if (type == ParameterType.STRING_LIST) {
			this.value = list;
		} else {
			throw new IllegalArgumentException(name + " is not a list parameter. ");
		}
	}

	public void setStringValue(String value) {
		if (type == ParameterType.STRING) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a string parameter.");
		}
	}

	public String getParamAsString() {
		switch (type) {
		case COLOR :
			Color c = getColorValue();
			return name + EQ + c.getRed() + ',' + c.getGreen() + ',' + c.getBlue() + LF;
		case FONT:
			return name + EQ + getFontAsString(getFontValue()) + LF;
		case STRING_LIST:
			return name + EQ + getStringListParamAsString() + LF;
		case INDEXED_STRING:
			List<String> list = getIndexedStringValue();
			StringBuilder buf = new StringBuilder();
			for (int i=0; i<list.size(); i++) {
				buf.append(name + DOT + i + EQ + list.get(i) + LF);
			}
			return buf.toString();
		default:
			return name + EQ + (value != null ? String.valueOf(value) : "") + LF;
		}
	}

	public static String getFontAsString(Font font) {
		if (font != null) {
			return font.getName() + DELIM + font.getStyle() + DELIM + font.getSize();
		} else {
			return Utility.EMPTY;
		}
	}

	public static String getFontAsDisplayString(Font font) {
		StringBuilder buf = new StringBuilder();
		if (font != null) {
			buf.append(font.getName()).append('-');
			buf.append((font.getStyle() == Font.PLAIN) ? "Plain" : Utility.EMPTY);
			buf.append((font.getStyle() & Font.BOLD) != 0 ? "Bold" : Utility.EMPTY);
			buf.append((font.getStyle() & Font.ITALIC) != 0 ? "Italic" : Utility.EMPTY);
			buf.append('-').append(font.getSize());
		}
		return buf.toString();
	}

	public String getStringListParamAsString() {
		return getStringListValue().stream().collect(Collectors.joining(DELIM));
	}

}
