package droid64.gui;

import org.junit.Test;

import droid64.db.SearchResultRow;
import org.junit.Assert;

public class SearchResultTableModelTest {

	@Test
	public void test() {
		SearchResultTableModel model = new SearchResultTableModel();
		model.updateDirEntry(new SearchResultRow("path1", "disk1", "label1", "file1", "type1", 101, "host1"));
		model.updateDirEntry(new SearchResultRow("path2", "disk2", "label2", "file2", "type2", 102, "host2"));
		Assert.assertEquals("",2, model.getRowCount());
		Assert.assertEquals("",7, model.getColumnCount());
		Assert.assertEquals("","path2", model.getValueAt(1, 0));
		model.getValueAt(1, 1);
		model.getValueAt(1, 2);
		model.getValueAt(1, 3);
		model.getValueAt(1, 4);
		model.getValueAt(1, 5);
		model.getValueAt(1, 6);
		model.getValueAt(-1, -1);
		model.getValueAt(1000, 1);
		Assert.assertEquals("", "Disk", model.getColumnHeader(1));
		model.clear();
	}

}
