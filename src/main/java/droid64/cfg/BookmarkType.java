package droid64.cfg;

import java.util.stream.Stream;

/** The Bookmark types */
public enum BookmarkType {
	/** 1. The root element */
	ROOT(1),
	/** 2. Directory bookmark */
	DIRECTORY(2),
	/** 3. Diskimage bookmark */
	DISKIMAGE(3),
	/** 4. Bookmark separator */
	SEPARATOR(4),
	/** 5. Bookmark folder */
	FOLDER(5);

	public final int type;

	/** Hidden constructor */
	private BookmarkType(int type) {
		this.type = type;
	}

	public static String[] getNames() {
		var values = values();
		var names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			names[i] = values[i].name();
		}
		return names;
	}

	public static Stream<BookmarkType> stream() {
		return Stream.of(values());
	}

	public static BookmarkType get(String name) {
		for (var bt : values()) {
			if (bt.name().equals(name)) {
				return bt;
			}
		}
		throw new IllegalArgumentException(name);
	}
}
