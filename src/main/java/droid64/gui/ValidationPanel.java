package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import droid64.d64.Utility;
import droid64.d64.ValidationError;

public class ValidationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String HEADER_TRACK_SECTOR = "\nTrack/Sector\n------------\n";
	private static final String HEADER_TRACK_SECTOR_FILE = "\nTrack/Sector\t File\n------------\t ----------------\n";

	private final EnumMap<ValidationError.Error, List<ValidationError>> errorMap = new EnumMap<>(ValidationError.Error.class);
	private final JButton repairButton = new JButton("Repair");
	private final JTextArea textArea = new JTextArea();
	private final Frame parentFrame;
	private final JButton closeButton = new JButton("Close");
	private JDialog dialog;
	private JCheckBox[] boxes;
	private List<ValidationError.Error> keys;
	private DiskPanel diskPanel;

	public ValidationPanel(Frame parentFrame) {
		this.parentFrame = parentFrame;
		this.dialog = new JDialog(parentFrame, "Validation", true);

		var columnModel = new DefaultTableColumnModel();
		var col1 = new TableColumn(0, 10);
		col1.setHeaderValue(Utility.EMPTY);
		columnModel.addColumn(col1);
		var col2 = new TableColumn(1, 30);
		col2.setHeaderValue("Error code");
		columnModel.addColumn(col2);
		var col3 = new TableColumn(2, 30);
		col3.setHeaderValue("Error count");
		columnModel.addColumn(col3);
		var col4 = new TableColumn(3, 80);
		col4.setHeaderValue("Error text");
		columnModel.addColumn(col4);

		textArea.setLineWrap(true);
		textArea.setEditable(false);

		var errorTable = new JTable(new ValidationTableModel(), columnModel);
		errorTable.getTableHeader().setReorderingAllowed(false);
		errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		errorTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				int row = errorTable.rowAtPoint(me.getPoint());
				if (row >= 0) {
					textArea.setText(getErrorDataString(keys.get(row), errorMap));
				}
			}
		});

		repairButton.setToolTipText("Repair selected validation errors.");
		repairButton.addActionListener(ae -> repair(errorTable, textArea));

		var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setTopComponent(new JScrollPane(errorTable));
		splitPane.setBottomComponent(new JScrollPane(textArea));

		var buttonPanel = new JPanel();
		buttonPanel.add(repairButton);
		buttonPanel.add(closeButton);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		GuiHelper.setSize(this, 4, 2);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void show(List<ValidationError> errors, DiskPanel diskPanel) {
		this.diskPanel = diskPanel;
		parseErrors(errors);
		repairButton.setEnabled(!errorMap.isEmpty());
		if (errorMap.isEmpty()) {
			textArea.setText("No validation errors found.");
		}
		GuiHelper.setPreferredSize(this, 2, 2);
		closeButton.addActionListener(e -> dialog.dispose());

		dialog.setTitle("Validation errors (" + diskPanel.getDiskImageType() + ")");
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parentFrame);
		dialog.setVisible(true);
	}

	private void repair(JTable errorTable, JTextArea textArea) {
		if (errorMap.isEmpty()) {
			return;
		}
		var repairList = new ArrayList<ValidationError.Error>();
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].isSelected()) {
				repairList.add(keys.get(i));
			}
		}
		if (!repairList.isEmpty()) {
			var errList = diskPanel.repairValidationErrors(repairList);
			parseErrors(errList);
			textArea.setText(Utility.EMPTY);
			repairButton.setEnabled(!errorMap.isEmpty());
			errorTable.invalidate();
			errorTable.repaint();
		}
	}

	protected void parseErrors(List<ValidationError> errorList) {
		errorMap.clear();
		if (errorList != null) {
			errorList.forEach(error -> {
				var code = error.getError();
				if (!errorMap.containsKey(code)) {
					errorMap.put(code, new ArrayList<>());
				}
				errorMap.get(code).add(error);
			});
		}
		boxes = new JCheckBox[errorMap.size()];
		for (var i = 0; i < boxes.length; i++) {
			boxes[i] = new JCheckBox();
		}
		var keyArr = errorMap.keySet().toArray(new ValidationError.Error[errorMap.size()]);
		Arrays.sort(keyArr);
		keys = Arrays.asList(keyArr);
	}

	private String getErrorDataString(ValidationError.Error key,
			Map<ValidationError.Error, List<ValidationError>> errorMap) {
		if (key == null) {
			return null;
		}
		var errorList = errorMap.get(key);
		if (errorList == null) {
			return null;
		}
		boolean hasFile = hasFile(key);
		var buf = new StringBuilder();
		buf.append(key);
		buf.append(hasFile ? HEADER_TRACK_SECTOR_FILE : HEADER_TRACK_SECTOR);
		int i = 0;
		for (var error : errorList) {
			if (hasFile) {
				buf.append(String.format("%3d/%-3d %s%n", error.getTrack(), error.getSector(), error.getFileName()));
			} else {
				buf.append(
						String.format("%3d/%-3d%s", error.getTrack(), error.getSector(), (i++ & 7) == 7 ? "\n" : " "));
			}
		}
		return buf.toString();
	}

	private boolean hasFile(ValidationError.Error key) {
		switch (key) {
		case ERROR_PARTITIONS_UNSUPPORTED:
		case ERROR_FILE_SECTOR_OUTSIDE_IMAGE:
		case ERROR_FILE_SECTOR_ALREADY_SEEN:
		case ERROR_FILE_SECTOR_ALREADY_USED:
		case ERROR_FILE_SECTOR_ALREADY_FREE:
			return true;
		default:
			return false;
		}
	}

	protected void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	class ValidationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return errorMap.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return Boolean.valueOf(boxes[row].isSelected());
			case 1:
				return keys.get(row).toString();
			case 2:
				return Integer.toString(errorMap.get(keys.get(row)).size());
			case 3:
				return keys.get(row);
			default:
				return Utility.EMPTY;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (value instanceof Boolean && column == 0 && row < boxes.length && boxes[row] != null) {
				boxes[row].setSelected((Boolean) value);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return column == 0 ? Boolean.class : String.class;
		}
	}

}
