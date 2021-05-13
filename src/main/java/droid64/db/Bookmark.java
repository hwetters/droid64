package droid64.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import droid64.d64.DiskImageType;
import droid64.d64.Utility;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bookmark", propOrder = { "name", "path", "created", "bookmarkType", "childs", "selectedNo", "pluginNo", "diskImageType", "extArguments", "notes", "zipped" })
public class Bookmark extends Value implements Serializable {
	private static final long serialVersionUID = -1L;

	public static final String SEPARATOR_STRING = Utility.repeat("\u2015", 16);

	@XmlElement(required = true)
	private String name;
	@XmlElement(required = true)
	private String path;
	@XmlElement(required = true, type = String.class)
	@XmlJavaTypeAdapter(DateAdapter.class)
	@XmlSchemaType(name = "created")
	private Date created;
	@XmlElement(required = true)
	BookmarkType bookmarkType;
	@XmlElementWrapper(name="childs", required = false)
	@XmlElement(name = "Bookmark")
	private List<Bookmark> childs;
	@XmlElement(required = false, defaultValue = "-1")
	private int selectedNo = -1;
	@XmlElement(required = false, defaultValue = "-1")
	private int pluginNo = -1;
	@XmlElement(required = false)
	private DiskImageType diskImageType;
	@XmlElement(required = false)
	private String extArguments;
	@XmlElement(required = false)
	private String notes;
	@XmlElement(required = false, defaultValue = "false")
	private boolean zipped;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public BookmarkType getBookmarkType() {
		return bookmarkType;
	}

	public void setBookmarkType(BookmarkType bookmarkType) {
		this.bookmarkType = bookmarkType;
	}

	public List<Bookmark> getChilds() {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		return childs;
	}

	public int getSelectedNo() {
		return selectedNo;
	}

	public void setSelectedNo(int selectedNo) {
		this.selectedNo = selectedNo;
	}

	public int getPluginNo() {
		return pluginNo;
	}

	public void setPluginNo(int pluginNo) {
		this.pluginNo = pluginNo;
	}

	public DiskImageType getDiskImageType() {
		return diskImageType;
	}

	public void setDiskImageType(DiskImageType diskImageType) {
		this.diskImageType = diskImageType;
	}

	public String getExtArguments() {
		return extArguments;
	}

	public void setExtArguments(String extArguments) {
		this.extArguments = extArguments;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isZipped() {
		return zipped;
	}

	public void setZipped(boolean zipped) {
		this.zipped = zipped;
	}

	@Override
	public String toString() {
		return bookmarkType == BookmarkType.SEPARATOR ? SEPARATOR_STRING : name;
	}

	@XmlType(name = "BookmarkType")
	@XmlEnum
	public enum BookmarkType {
		@XmlEnumValue("ROOT")
		ROOT(1),
		@XmlEnumValue("DIRECTORY")
		DIRECTORY(2),
		@XmlEnumValue("DISKIMAGE")
		DISKIMAGE(3),
		@XmlEnumValue("SEPARATOR")
		SEPARATOR(4),
		@XmlEnumValue("FOLDER")
		FOLDER(5);

		public final int type;

		private BookmarkType(int type) {
			this.type = type;
		}

		public static String[] getNames() {
			var values = values();
			var names = new String[values.length];
			for (int i=0; i < values.length; i++) {
				names[i] = values[i].name();
			}
			return names;
		}
		public static Stream<BookmarkType> stream() {
			return Stream.of(values());
		}
	}
}
