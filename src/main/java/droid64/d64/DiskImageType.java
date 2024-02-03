package droid64.d64;
import java.util.stream.Stream;

import droid64.gui.ConsoleStream;


public enum DiskImageType {
	UNDEFINED(0, "Undefined", "Undefined image", "", "", "1541", 0, true, null),
	D64(1, "D64", "D64 (C1541)", "Normal D64 (C1541 5.25\") image", "2A", "1541", 174848, false, D64.class),
	D71(2, "D71", "D71 (C1571)", "Normal D71 (C1571 5.25\") image", "2A", "1571", 349696, false, D71.class),
	D81(3, "D81", "D81 (C1581)", "Normal D81 (C1581 3.5\") image", "3D", "1581", 819200, false, D81.class),
	T64(4, "T64", "T64 (C1530)", "Normal T64 (tape) image", "", "1530", 0, false, T64.class),
	D64_CPM_C64(5, "D64 CP/M (C64)", "CP/M D64 (C1541)", "CP/M for C64 on D64 (C1541 5.25\") image", "2A", "1541", 174848, true, D64.class),
	D64_CPM_C128(6, "D64 CP/M (C128)", "CP/M D64 (C1541)", "CP/M for C128 on D64 (C1541 5.25\") image", "2A", "1541", 349696, true, D64.class),
	D71_CPM(7, "D71 CP/M", "CP/M D71 (C1571)", "CP/M for C128 on D71 (C1571 5.25\") image", "2A", "1571", 349696, true, D71.class),
	D81_CPM(8, "D81 CP/M", "CP/M D81 (C1581)", "CP/M on D81 (C1581 3.5\") image", "3D", "1581", 819200, true, D81.class),
	D82(9,  "D82", "D82 (C8250)", "Normal D81 (C8250 5.25\") image", "2A", "8250", 1066496, false, D82.class),
	D80(10, "D80", "D80 (C8050)", "Normal D81 (C8050 5.25\") image", "2A", "8050", 533248, false, D80.class),
	D67(11, "D67", "D67 (C2040)", "Normal D81 (C2040 5.25\") image", "2A", "2040", 176640, false, D67.class),
	LNX(12, "LNX", "Lynx", "Normal Lynx archive", "2A", "1541", 0, true, LNX.class),
	D88(13, "D88", "D88 (C8280)", "Normal D88 (C8280 8\") image", "3A", "8280", 1025024, false, D88.class),
	D90_9060(14, "D90_9060", "D90 (C9060)", "Normal D90 (D9060) image", "3A", "9000", D90.D9060_SIZE, false, D90.class),
	D90_9090(15, "D90_9090", "D90 (C9090)", "Normal D90 (D9090) image", "3A", "9000", D90.D9090_SIZE, false, D90.class);

	public final int type;
	public final String id;
	public final String longName;
	public final String description;
	public final String dosVersion;
	public final String driveName;
	public final int expectedSize;
	public final boolean readonly;
	public final Class<? extends DiskImage> clazz;

	private DiskImageType(int type, String id, String longName, String description, String dosVersion, String driveName, int expectedSize, boolean readonly, Class<? extends DiskImage> clazz) {
		this.type = type;
		this.id = id;
		this.longName = longName;
		this.description = description;
		this.dosVersion = dosVersion;
		this.driveName = driveName;
		this.expectedSize = expectedSize;
		this.readonly = readonly;
		this.clazz = clazz;
	}

	public static String[] getNames() {
		return stream().map(dt->dt.id).toArray(String[]::new);
	}

	public static Stream<DiskImageType> stream() {
		return Stream.of(values());
	}
	public boolean isReadonly() {
		return readonly;
	}
	
	@Override
	public String toString() {
		return id;
	}

	public static DiskImageType get(int type) {
		return stream().filter(ft -> ft.type == type).findFirst().orElse(UNDEFINED);
	}

	public DiskImage getInstance(ConsoleStream consoleStream) throws CbmException {
		if (clazz == null) {
			throw new CbmException("Unknown file format.");
		}
		try {
			return clazz.getConstructor(DiskImageType.class, ConsoleStream.class).newInstance(this, consoleStream);
		} catch (Exception e) {
			throw new CbmException("Failed to create " + id + " instance.", e);
		}
	}

	public DiskImage getInstance(byte[] imageData, ConsoleStream consoleStream) throws CbmException {
		if (clazz == null) {
			throw new CbmException("Unknown file format.");
		}
		try {
			return clazz.getConstructor(DiskImageType.class, byte[].class, ConsoleStream.class).newInstance(this, imageData, consoleStream);
		} catch (Exception e) {
			throw new CbmException("Failed to create " + id + " instance.", e);
		}
	}

	public static DiskImageType get(String name) {
		for (var dt : values()) {
			if (dt.name().equals(name)) {
				return dt;
			}
		}
		return UNDEFINED;
	}
}
