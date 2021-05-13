package droid64.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "timestamp", "bookmarks" })
@XmlRootElement(name = "BookmarkList")
public class BookmarkList implements Serializable {

	private static final long serialVersionUID = -1L;
	@XmlElement(name = "name", defaultValue = "Bookmarks")
	private String name;
	@XmlElement(required = false, type = String.class)
	@XmlJavaTypeAdapter(DateAdapter.class)
	@XmlSchemaType(name = "timestamp")
	private Date timestamp;
	@XmlElement(name = "Bookmark")
	private List<Bookmark> bookmarks;


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
}
