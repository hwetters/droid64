package droid64.d64;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FileType")
@XmlEnum
public enum FileType {
	@XmlEnumValue("DEL")
	DEL(0, "DEL file"),
	@XmlEnumValue("SEQ")
	SEQ(1, "SEQ file"),
	@XmlEnumValue("PRG")
	PRG(2, "PRG file"),
	@XmlEnumValue("USR")
	USR(3, "USR file"),
	@XmlEnumValue("REL")
	REL(4, "REL file"),
	@XmlEnumValue("CBM")
	CBM(5, "CBM partition file");

	public final int type;
	public final String description;
	private FileType(int type, String description) {
		this.type = type;
		this.description = description;
	}
	public static FileType get(int type) {
		for (FileType ft : values()) {
			if (ft.type == type) {
				return ft;
			}
		}
		return null;
	}
	public static String[] getNames() {
		FileType[] values = values();
		String[] names = new String[values.length];
		for (int i=0; i < values.length; i++) {
			names[i] = values[i].name();
		}
		return names;
	}
	public static Stream<FileType> stream() {
		return Stream.of(values());
	}
}
