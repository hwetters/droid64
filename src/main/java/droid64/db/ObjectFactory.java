package droid64.db;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	/**
	 * Create an instance of {@link DiskList }
	 *
	 * @return DiskList
	 */
	public DiskList createDiskList() {
		return new DiskList();
	}

	/**
	 * Create an instance of {@link Disk }
	 *
	 * @return Disk
	 */
	public Disk createDisk() {
		return new Disk();
	}

	/**
	 * Create an instance of {@link DiskFile }
	 *
	 * @return DiskFile
	 */
	public DiskFile createDiskFile() {
		return new DiskFile();
	}

	/**
	 * Create an instance of {@link BookmarkList }
	 *
	 * @return BookmarkList
	 */
	public BookmarkList createBookmarkList() {
		return new BookmarkList();
	}

	/**
	 * Create an instance of {@link Bookmark }
	 *
	 * @return Bookmark
	 */
	public Bookmark createBookmark() {
		return new Bookmark();
	}
}
