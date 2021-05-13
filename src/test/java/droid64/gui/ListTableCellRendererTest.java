package droid64.gui;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.CbmFile;
import droid64.d64.DirEntry;

public class ListTableCellRendererTest {

	@Test
	public void test() {
		EntryTableModel model = new EntryTableModel();
		model.setMode(EntryTableModel.MODE_LOCAL);
		model.updateDirEntry(new DirEntry(new CbmFile(), 0));
		ListTableCellRenderer renderer = new ListTableCellRenderer(model);
		Assert.assertNotNull("", renderer.getTableCellRendererComponent(new JTable(), "value", true, false, 0, 0));
	}

}
