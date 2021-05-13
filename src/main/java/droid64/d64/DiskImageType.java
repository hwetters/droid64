package droid64.d64;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import droid64.gui.ConsoleStream;

@XmlType(name = "DiskImageType")
@XmlEnum
public enum DiskImageType {
	@XmlEnumValue("UNDEFINED")
	UNDEFINED(0, "Undefined", "Undefined image", "", "", "1541", 0, null),
	@XmlEnumValue("D64")
	D64(1, "D64", "D64 (C1541)", "Normal D64 (C1541 5.25\") image", "2A", "1541", 174848, D64.class),
	@XmlEnumValue("D71")
	D71(2, "D71", "D71 (C1571)", "Normal D71 (C1571 5.25\") image", "2A", "1571", 349696, D71.class),
	@XmlEnumValue("D81")
	D81(3, "D81", "D81 (C1581)", "Normal D81 (C1581 3.5\") image", "3D", "1581", 819200, D81.class),
	@XmlEnumValue("T64")
	T64(4, "T64", "T64 (C1530)", "Normal T64 (tape) image", "", "1530", 0, T64.class),
	@XmlEnumValue("D64_CPM_C64")
	D64_CPM_C64(5, "D64 CP/M (C64)", "CP/M D64 (C1541)", "CP/M for C64 on D64 (C1541 5.25\") image", "2A", "1541", 174848, D64.class),
	@XmlEnumValue("D64_CPM_C128")
	D64_CPM_C128(6, "D64 CP/M (C128)", "CP/M D64 (C1541)", "CP/M for C128 on D64 (C1541 5.25\") image", "2A", "1541", 349696,D64.class),
	@XmlEnumValue("D71_CPM")
	D71_CPM(7, "D71 CP/M", "CP/M D71 (C1571)", "CP/M for C128 on D71 (C1571 5.25\") image", "2A", "1571", 349696, D71.class),
	@XmlEnumValue("D81_CPM")
	D81_CPM(8, "D81 CP/M", "CP/M D81 (C1581)", "CP/M on D81 (C1581 3.5\") image", "3D", "1581", 819200, D81.class),
	@XmlEnumValue("D82")
	D82(9,  "D82", "D82 (C8250)", "Normal D81 (C8250 5.25\") image", "2A", "8250", 1066496, D82.class),
	@XmlEnumValue("D80")
	D80(10, "D80", "D80 (C8050)", "Normal D81 (C8050 5.25\") image", "2A", "8050", 533248, D80.class),
	@XmlEnumValue("D67")
	D67(11, "D67", "D67 (C2040)", "Normal D81 (C2040 5.25\") image", "2A", "2040", 176640, D67.class),
	@XmlEnumValue("LNX")
	LNX(12, "LNX", "Lynx", "Normal Lynx archive", "2A", "1541", 0, LNX.class),
	@XmlEnumValue("D88")
	D88(13, "D88", "D88 (C8280)", "Normal D88 (C8280 8\") image", "3A", "8280", 1025024, D88.class);

	public final int type;
	public final String id;
	public final String longName;
	public final String description;
	public final String dosVersion;
	public final String driveName;
	public final int expectedSize;
	public final Class<? extends DiskImage> clazz;

	private DiskImageType(int type, String id, String longName, String description, String dosVersion, String driveName, int expectedSize, Class<? extends DiskImage> clazz) {
		this.type = type;
		this.id = id;
		this.longName = longName;
		this.description = description;
		this.dosVersion = dosVersion;
		this.driveName = driveName;
		this.expectedSize = expectedSize;
		this.clazz = clazz;
	}

	public static String[] getNames() {
		DiskImageType[] values = values();
		String[] names = new String[values.length];
		for (int i=0; i < values.length; i++) {
			names[i] = values[i].id;
		}
		return names;
	}

	public static Stream<DiskImageType> stream() {
		return Stream.of(values());
	}

	@Override
	public String toString() {
		return id;
	}

	public static DiskImageType get(int type) {
		for (DiskImageType ft : values()) {
			if (ft.type == type) {
				return ft;
			}
		}
		return UNDEFINED;
	}

	public DiskImage getInstance(ConsoleStream consoleStream) throws CbmException {
		if (clazz == null) {
			throw new CbmException("Unknown file format.");
		}
		try {
			DiskImage img = clazz.getConstructor(ConsoleStream.class).newInstance(consoleStream);
			img.setImageFormat(this);
			return img;
		} catch (Exception e) {
			throw new CbmException("Failed to create " + id + " instance.", e);
		}
	}

	public DiskImage getInstance(byte[] imageData, ConsoleStream consoleStream) throws CbmException {
		if (clazz == null) {
			throw new CbmException("Unknown file format.");
		}
		try {
			DiskImage img =  clazz.getConstructor(byte[].class, ConsoleStream.class).newInstance(imageData, consoleStream);
			img.setImageFormat(this);
			return img;
		} catch (Exception e) {
			throw new CbmException("Failed to create " + id + " instance.", e);
		}
	}
}
