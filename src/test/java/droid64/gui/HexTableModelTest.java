package droid64.gui;

import javax.swing.text.BadLocationException;

import org.junit.Assert;
import org.junit.Test;

public class HexTableModelTest {

	@Test
	public void test() throws BadLocationException {
		byte[] data = "0123456789abcdefABCDEFGHIJKLMNOPQRSTUV\n\r\tÅÄÖ".getBytes();
		HexTableModel model = new HexTableModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void setValue(int pos, int value) {
			}};
		model.loadData(data, data.length);
		model.getColumnCount();
		model.getRowCount();
		model.getColumnName(0);
		model.getColumnName(1);
		model.getColumnName(16);
		model.getColumnName(17);
		model.getColumnName(18);
		model.setReadOnly(false);
		Assert.assertFalse(model.isReadOnly());
		model.setValueAt(Integer.valueOf(0), 0, 0);
		model.setValueAt("X", 0, 1);
		model.getValueAt(0, 16);
		model.getValueAt(999, 16);
		model.setDirty(true);
		Assert.assertFalse("editable", model.isCellEditable(0, 0));

		Assert.assertTrue(model.isDirty());
		Assert.assertNotNull(model.getValueAt(0, 0));
		model.getByteAt(1, 1);
		Assert.assertNotNull(model.getValueAt(0, 1));
		Assert.assertNotNull(model.getValueAt(0, 2));
		Assert.assertNotNull(model.getValueAt(0, 15));
		Assert.assertNotNull(model.getValueAt(0, 16));
		Assert.assertNotNull(model.getValueAt(0, 17));
		Assert.assertNotNull(model.getValueAt(1, 2));
		Assert.assertNotNull(model.getValueAt(1, 15));
		Assert.assertNotNull(model.getValueAt(1, 16));
		Assert.assertNotNull(model.getValueAt(1, 17));
		model.loadData(data, data.length);
		model.loadData(null, 1);
	}

}
