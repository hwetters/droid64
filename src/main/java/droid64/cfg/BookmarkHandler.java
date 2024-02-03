package droid64.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import droid64.d64.DiskImageType;
import droid64.d64.Utility;

/**
 * BookmarkHandler
 */
public class BookmarkHandler extends DefaultHandler {

	private final Deque<Bookmark> stack = new ArrayDeque<>();
	private BookmarkList root = null;
	private StringBuilder data = null;
	private Bookmark current = null;
	boolean bTimestamp = false;
	boolean bName = false;

	/** Constructor */
	public BookmarkHandler() {
		super();
	}

	/** @return The root BookmarkList element */
	public BookmarkList getBookmarkList() {
		return root;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (root == null) {
			if (TagName.BOOKMARK_LIST.match(qName)) {
				root = new BookmarkList();
			} else {
				throw new SAXException("First element must be " + TagName.BOOKMARK_LIST);
			}
		} else {

			if (current == null) {
				switch (TagName.get(qName)) {
				case TIMESTAMP:
					bTimestamp = true;
					break;
				case BOOKMARK:
					var n = new Bookmark();
					root.getBookmarks().add(n);
					current = n;
					break;
				case NAME:
					bName = true;
					break;
				default:
					throw new SAXException("Unexpected element " + qName);
				}
			} else if (TagName.BOOKMARK.match(qName)) {
				var n = new Bookmark();
				stack.push(current);
				current.getChilds().add(n);
				current = n;
			} else {
				switch (TagName.get(qName)) {
				case NAME:
				case PATH:
				case BOOKMARK_TYPE:
				case SELECTED_NO:
				case PLUGIN_NO:
				case ZIPPED:
				case DISK_IMAGE_TYPE:
				case CREATED:
				case NOTES:
				case EXT_ARGUMENTS:
					break;
				case BOOKMARK_LIST:
					throw new SAXException("Only the first element can be " + TagName.BOOKMARK_LIST);
				default:
					throw new SAXException("Unknown element " + qName);
				}
			}
		}
		// create the data container
		data = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		var v = data.toString();

		if (current == null) {
			switch (TagName.get(qName)) {
			case BOOKMARK_LIST:
				// reached end.
				break;
			case TIMESTAMP:
				root.setTimestamp(parseDate(v));
				bTimestamp = false;
				break;
			case NAME:
				root.setName(v);
				break;
			case BOOKMARK:
				throw new SAXException("Orphan closing " + TagName.BOOKMARK + " element");
			default:
				throw new SAXException("Unexpected closing element " + qName);
			}
		} else {
			switch (TagName.get(qName)) {
			case BOOKMARK:
				current = stack.isEmpty() ? null : stack.pop();
				break;
			case NAME:
				current.setName(v);
				break;
			case PATH:
				current.setPath(v);
				break;
			case BOOKMARK_TYPE:
				current.setBookmarkType(BookmarkType.get(v));
				break;
			case SELECTED_NO:
				current.setSelectedNo(Integer.valueOf(v));
				break;
			case PLUGIN_NO:
				current.setPluginNo(Integer.valueOf(v));
				break;
			case ZIPPED:
				current.setZipped(Boolean.valueOf(v));
				break;
			case DISK_IMAGE_TYPE:
				current.setDiskImageType(DiskImageType.get(v));
				break;
			case CREATED:
				current.setCreated(parseDate(v));
				break;
			case NOTES:
				current.setNotes(v);
				break;
			case EXT_ARGUMENTS:
				current.setExtArguments(v);
				break;
			case TIMESTAMP:
				root.setTimestamp(parseDate(v));
				bTimestamp = false;
				break;
			case BOOKMARK_LIST:
				throw new SAXException("Unexpected closing BookmarkList element");
			}
		}
	}

	private Date parseDate(String str) throws SAXException {
		try {
			return Utility.parseISO(str);
		} catch (Exception e) {
			throw new SAXException("No date: '" + str + "' " + e.getMessage());
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		data.append(new String(ch, start, length));
	}

	/**
	 * Load XML file using the handler
	 *
	 * @param file the XML file
	 * @return BoommarkList
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public BookmarkList readXML(File file)
			throws IOException, ParserConfigurationException, SAXException {

		try (var is = new FileInputStream(file)) {
			var spf = SAXParserFactory.newInstance();
			spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			var saxParser = spf.newSAXParser();
			saxParser.parse(new InputSource(is), this);
			return getBookmarkList();
		}
	}
}
