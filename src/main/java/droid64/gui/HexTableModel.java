package droid64.gui;

import javax.swing.table.AbstractTableModel;

import droid64.d64.Utility;

/**
 * Table model used by HexViewFrame.
 * @author Henrik
 * @see HexViewPanel
 */
public abstract class HexTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final int BYTES_PER_ROW = 16;
	private byte[] data = null;
	private int length = 0;
	private boolean dirty;
	private boolean readOnly = true;

	public void loadData(byte[] data, int length) {
		this.data = data;
		this.length = data != null ? Math.min(data.length, length) : 0;
		fireTableDataChanged();
		dirty = false;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Address";
		} else if (column <= BYTES_PER_ROW) {
			return Integer.toHexString(column - 1).toUpperCase();
		} else if (column == BYTES_PER_ROW + 1) {
			return "ASCII";
		} else {
			return Utility.EMPTY;
		}
	}

	@Override
	public int getRowCount() {
		if (data == null || data.length < 1) {
			return 0;
		} else {
			return (length + BYTES_PER_ROW  - 1) / BYTES_PER_ROW ;
		}
	}

	@Override
	public int getColumnCount() {
		return BYTES_PER_ROW + 2;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		if (data == null) {
			return Utility.EMPTY;
		} else {
			if (columnIndex == 0) {
				return Utility.getIntHexString(rowIndex * BYTES_PER_ROW).toUpperCase();
			} else if (columnIndex == BYTES_PER_ROW + 1) {
				return getDumpRowString(rowIndex);
			} else {
				int addr = rowIndex * BYTES_PER_ROW + columnIndex -1;
				if (addr < data.length && addr < length) {
					return Utility.getByteStringUpperCase(data[addr]);
				} else {
					return Utility.EMPTY;
				}
			}
		}
	}

	public Integer getByteAt(int rowIndex, int columnIndex) {
		if (data == null || columnIndex == 0 || columnIndex == BYTES_PER_ROW + 1) {
			return null;
		} else {
			int addr = rowIndex * BYTES_PER_ROW + columnIndex -1;
			if (addr < data.length && addr < length) {
				return Integer.valueOf(data[addr] & 0xff);
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return !readOnly && col > 0 && col <= BYTES_PER_ROW && row != -1;
	}

	public abstract void setValue(int pos, int value);

	@Override
	public void setValueAt(Object obj, int row, int col) {
		if (isCellEditable(row, col)) {
			int addr = row * BYTES_PER_ROW + col -1;
			int value = Utility.parseHexInteger((String)obj, data[addr]) & 0xff;
			data[addr] = (byte) (value & 0xff);
			setValue(addr, value);
			dirty = true;
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Get an ASCII dump of the bytes displayed at rowIndex in data.
	 * @param rowIndex int
	 * @return String
	 */
	private String getDumpRowString(int rowIndex) {
		int start = rowIndex * BYTES_PER_ROW;
		if (data == null) {
			return "x";
		}
		var buf = new StringBuilder();
		for (int i=start; i < start + BYTES_PER_ROW; i++) {
			if (i < data.length) {
				byte b = data[i];
				if (b < 0x20 || b > 0x7e) {
					buf.append('.');
				} else {
					buf.append((char)b);
				}
			} else {
				buf.append(' ');
			}
		}
		return buf.toString();
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

}
