package droid64.cfg;

/**
 * XML Tag Names
 */
public enum TagName {
	/** BookmarkList */
	BOOKMARK_LIST("BookmarkList"),
	/** Timestamp */
	TIMESTAMP("timestamp"),
	/** Bookmark */
	BOOKMARK("bookmark"),
	/** Name */
	NAME("name"),
	/** Path */
	PATH("path"),
	/** Created */
	CREATED("created"),
	/** BookmarkType */
	BOOKMARK_TYPE("bookmarkType"),
	/** Notes */
	NOTES("notes"),
	/** SelectedNo */
	SELECTED_NO("selectedNo"),
	/** PluginNo */
	PLUGIN_NO("pluginNo"),
	/** DiskImageType */
	DISK_IMAGE_TYPE("diskImageType"),
	/** Zipped */
	ZIPPED("zipped"),
	/** ExtArguments */
	EXT_ARGUMENTS("extArguments");

	/** The label of the tag */
	final String label;

	/** Hidden Constructor */
	private TagName(String tagName) {
		this.label = tagName;
	}

	/** @return true when tagName equals (case insensitive) current tagName */
	public boolean match(String tagName) {
		return this.label.equalsIgnoreCase(tagName);
	}

	/**
	 * @return the TagName with the specified tagName
	 * @throws IllegalArgumentException if there is no such tagName
	 */
	public static TagName get(String label) {
		for(var t : values()) {
			if (t.label.equalsIgnoreCase(label)) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown tag "+label);
	}

	/** @return start tag */
	public String start() {
		return '<' + label+'>';
	}

	/** @return end tag */
	public String end() {
		return "</" + label+'>';
	}

	/** @return value between start and ending tag name */
	public String format(String value) {
		return start() + (value != null ? value : "") + end();
	}

	/** @return the tagName */
	@Override
	public String toString() {
		return label;
	}
}
