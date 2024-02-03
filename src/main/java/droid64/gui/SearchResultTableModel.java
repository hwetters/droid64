package droid64.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import droid64.d64.Utility;
import droid64.db.SearchResultRow;

/**
 * Table model to handle search results.
 * @see SearchPanel
 * @author Henrik
 */
public class SearchResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	/** Search results in a Vector of SearchResultRow */
	private final List<SearchResultRow> data = new ArrayList<>();
	/** Table headers */
	private static final String[] COL_HEADS =	{ "Path", "Disk", "Label", "File", "Type", "Size", "Host" };

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return COL_HEADS.length;
	}

	public String getColumnHeader(int column) {
		return COL_HEADS[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex >= data.size() || rowIndex < 0) {
				return Utility.EMPTY;
			}
			var row = data.get(rowIndex);
			switch (columnIndex) {
			case 0: return row.getPath();
			case 1: return row.getDisk();
			case 2: return row.getLabel();
			case 3:	return row.getFile();
			case 4: return row.getType();
			case 5: return row.getSize();
			case 6: return row.getHostName();
			default: return Utility.EMPTY;
			}
		} catch (ArrayIndexOutOfBoundsException  e) {	//NOSONAR
			return Utility.EMPTY;
		}
	}

	/**
	 * Clear table
	 */
	public void clear() {
		int oldSize = data.size();
		data.clear();
		fireTableRowsDeleted(0, oldSize);
	}

	/**
	 * Add row to table
	 * @param row search result row
	 */
	public void updateDirEntry(SearchResultRow row) {
		data.add(row);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}

}
