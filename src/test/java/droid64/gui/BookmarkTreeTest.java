package droid64.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.Assert;
import org.junit.Test;

import droid64.cfg.Bookmark;
import droid64.cfg.BookmarkType;

public class BookmarkTreeTest {

	private static final File BOOKMARK_FILE = new File("src/test/resources/droid64/gui/bookmarks.xml");

	@Test
	public void test() throws IOException {
		var tmp =  getTempFile("_book.xml", true);
		Files.copy(BOOKMARK_FILE.toPath(), tmp.toPath(),  StandardCopyOption.REPLACE_EXISTING);
		var consoleStream = new ConsoleStream(new JTextArea());

		var bt = new BookmarkTree(new MainPanel(new JFrame()), consoleStream);
		bt.load(new File("/tmp/doid64/test/missing_bookmark_file.xml"));
		Assert.assertEquals("menuitems.size 0", 0, bt.getMenuItems().size());
		bt.load(tmp);
		Assert.assertEquals("menuitems.size 1", 6, bt.getMenuItems().size());

		var b1 = new Bookmark();
		b1.setBookmarkType(BookmarkType.DIRECTORY);
		bt.addEntry(b1, null);
		Assert.assertEquals("menuitems.size 2", 7, bt.getMenuItems().size());

		var b2 = new Bookmark();
		b2.setBookmarkType(BookmarkType.DIRECTORY);
		bt.addEntry(b2, (DefaultMutableTreeNode) bt.getModel().getRoot());
		Assert.assertEquals("menuitems.size 3", 8, bt.getMenuItems().size());
	}


	private File getTempFile(String suffix, boolean deleteOnExit) throws IOException {
		var tmpFile = File.createTempFile("UnitTest_", suffix);
		if (deleteOnExit) {
			tmpFile.deleteOnExit();
		}
		return tmpFile;
	}
}
