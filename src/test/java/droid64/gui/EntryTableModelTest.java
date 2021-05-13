package droid64.gui;

import javax.swing.text.BadLocationException;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.CbmFile;
import droid64.d64.DirEntry;

public class EntryTableModelTest {

	@Test
	public void test() throws BadLocationException {
		EntryTableModel model = new EntryTableModel();
		model.updateDirEntry(new DirEntry(new CbmFile(), 0));
		model.getColumnName(0);
		model.getColumnCount();
		model.getValueAt(0, 0);
		model.getValueAt(0, 1);
		model.getValueAt(0, 2);
		model.getValueAt(0, 3);
		model.getValueAt(0, 4);
		model.getValueAt(0, 5);
		model.getValueAt(0, 6);
		model.getValueAt(0, 7);
		model.getDirEntry(0);
		model.setMode(EntryTableModel.MODE_CPM);
		model.getColumnCount();
		model.getColumnName(0);
		model.getValueAt(0, 0);
		model.setMode(EntryTableModel.MODE_LOCAL);
		model.getColumnCount();
		model.getColumnName(0);
		model.getValueAt(0, 0);
		model.getValueAt(0, 1);
		model.getValueAt(0, 2);
		model.getValueAt(0, 3);
		Assert.assertNotNull("getValueAt",model.getValueAt(0, 4));
		model.setMode(-1);
		model.getTableColumnModel();
		Assert.assertNotNull("getMode" ,model.getMode());
		model.clear();;
		Assert.assertNull("getDirEntry", model.getDirEntry(0));
	}
}
