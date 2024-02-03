package droid64.d64;
import java.util.stream.Stream;

public enum FileType {
	DEL(0, "DEL file"),
	SEQ(1, "SEQ file"),
	PRG(2, "PRG file"),
	USR(3, "USR file"),
	REL(4, "REL file"),
	CBM(5, "CBM partition file");

	public final int type;
	public final String description;

	private FileType(int type, String description) {
		this.type = type;
		this.description = description;
	}

	public static FileType get(int type) {
		return stream().filter(ft -> ft.type == type).findFirst().orElse(null);
	}

	public static FileType get(String name) {
		return stream().filter(ft -> ft.name().equals(name)).findFirst().orElse(null);
	}

	public static String[] getNames() {
		return stream().map(FileType::name).toArray(String[]::new);
	}

	public static Stream<FileType> stream() {
		return Stream.of(values());
	}
}
