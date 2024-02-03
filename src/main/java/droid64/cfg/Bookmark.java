package droid64.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import droid64.d64.DiskImageType;
import droid64.d64.Utility;
import droid64.db.Value;

/**
 * Bookmark
 */
public class Bookmark extends Value implements Serializable {
	private static final long serialVersionUID = -1L;
	
	/** Bookmark separator label */
	public static final String SEPARATOR_STRING = Utility.repeat("\u2015", 16);

	private String name;
	private String path;
	private Date created;
	private BookmarkType bookmarkType;
	private List<Bookmark> childs;
	private int selectedNo = -1;
	private int pluginNo = -1;
	private DiskImageType diskImageType;
	private String extArguments;
	private String notes;
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

	/**
	 * @return the Bookmark as a XML string
	 */
	public String toXML() {

		var b = new StringBuilder();

		b.append(TagName.BOOKMARK.start());
		if (name != null && !name.isEmpty()) {
			b.append(TagName.NAME.format(name));
		}
		if (path != null && !path.isEmpty()) {
			b.append(TagName.PATH.format(path));
		}
		if (notes != null && !notes.isEmpty()) {
			b.append(TagName.NOTES.format(notes));
		}
		if (extArguments != null && !extArguments.isEmpty()) {
			b.append(TagName.EXT_ARGUMENTS.format(extArguments));
		}
		if (created != null) {
			b.append(TagName.CREATED.format(Utility.formatAsISO(created)));
		}
		if (bookmarkType != null) {
			b.append(TagName.BOOKMARK_TYPE.format(String.valueOf(bookmarkType)));
		}
		if (diskImageType != null) {
			b.append(TagName.DISK_IMAGE_TYPE.format(String.valueOf(diskImageType)));
		}
		if (selectedNo >= 0) {
			b.append(TagName.SELECTED_NO.format(String.valueOf(selectedNo)));
		}
		if (pluginNo >= 0) {
			b.append(TagName.PLUGIN_NO.format(String.valueOf(pluginNo)));
		}

		b.append(TagName.ZIPPED.format(String.valueOf(zipped)));
		b.append(getChilds().stream().map(Bookmark::toXML).collect(Collectors.joining()));
		b.append(TagName.BOOKMARK.end()).append('\n');

		return b.toString();
	}
}
