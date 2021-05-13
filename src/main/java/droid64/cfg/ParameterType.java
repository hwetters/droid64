package droid64.cfg;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
/** Parameter types */
@XmlType(name = "ParameterType")
@XmlEnum
public enum ParameterType {
	/** paramName=text */
	@XmlEnumValue("STRING")
	STRING(String.class),
	/** paramName=integer */
	@XmlEnumValue("INTEGER")
	INTEGER(Integer.class),
	/** paramName={ yes | no } */
	@XmlEnumValue("BOOLEAN")
	BOOLEAN(Boolean.class),
	/** paramName=r,g,b */
	@XmlEnumValue("COLOR")
	COLOR(Color.class),
	/** paramName=fontName */
	@XmlEnumValue("FONT")
	FONT(Font.class),
	/** paramName=string;string;string ... */
	@XmlEnumValue("STRING_LIST")
	STRING_LIST(List.class),
	/** paramName.x=value */
	@XmlEnumValue("INDEXED_STRING")
	INDEXED_STRING(List.class),
	/** paramName=filename */
	@XmlEnumValue("FILE")
	FILE(File.class);

	protected final Class<?> clazz;
	private ParameterType(Class<?> clazz) {
		this.clazz = clazz;
	}
}