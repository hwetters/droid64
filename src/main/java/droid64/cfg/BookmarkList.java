package droid64.cfg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import droid64.d64.Utility;

public class BookmarkList implements Serializable {

	private static final long serialVersionUID = -1L;
	private String name;
	private Date timestamp;
	private List<Bookmark> bookmarks;

	/** Constructor */
	public BookmarkList() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Bookmark> getBookmarks() {
		if (bookmarks == null) {
			bookmarks = new ArrayList<>();
		}
		return bookmarks;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return the BookmarkList as a XML string
	 */
	public String toXML() {
		return new StringBuilder()
				.append(TagName.BOOKMARK_LIST.start())
				.append(TagName.TIMESTAMP.format(Utility.formatAsISO(new Date())))
				.append(TagName.NAME.format(name))
				.append(getBookmarks().stream().map(Bookmark::toXML).collect(Collectors.joining()))
				.append(TagName.BOOKMARK_LIST.end()).append('\n').toString();
	}
}
