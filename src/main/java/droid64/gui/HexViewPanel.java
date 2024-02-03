package droid64.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.ProgramParser;
import droid64.d64.TrackSector;
import droid64.d64.Utility;

/**
 * Class used to show a hex dump of a byte array.
 *
 * @author Henrik
 */
public class HexViewPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String ASM_MODE = Utility.getMessage(Resources.DROID64_HEXVIEW_ASMMODE);
	private static final String HEX_MODE = Utility.getMessage(Resources.DROID64_HEXVIEW_HEXMODE);
	private final MainPanel mainPanel;
	private final JButton modeButton = new JButton(HEX_MODE);
	private final JTextArea asmTextArea = new JTextArea();
	private final JPanel cards = new JPanel(new CardLayout());
	private final HexTableModel model = new HexTableModel() {

		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(int index, int value) {
			diskImage.setCbmDiskValue(pos + index, value);
		}

	};
	private final JTable table = new JTable(model) {
		private static final long serialVersionUID = 1L;
		private final CustomCellRenderer renderer = new CustomCellRenderer();
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			return renderer;
		}
	};
	private final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
	private final JButton okButton = new JButton(Utility.getMessage(Resources.DROID64_HEXVIEW_CLOSE));
	private final JButton applyButton = new JButton(Utility.getMessage(Resources.DROID64_HEXVIEW_APPLY));

	private final boolean[] c64Modes = {false, false};
	private byte[] data;
	private final DiskImage diskImage;

	private JFormattedTextField trkField;
	private JFormattedTextField secField;
	private final JTextField offset = new JTextField("0", 8);

	private final JButton goBlockButton = new JButton("---:---");

	private final String title;
	private int selectedTrack = -1;
	private int selectedSector = -1;

	private int pos = 0;

	/**
	 * Constructor for disk image block
	 * @param title the title
	 * @param mainPanel the maninPanel
	 * @param track the track
	 * @param sector the sector
	 * @param diskImage the diskImage
	 * @throws CbmException when block could not be retrieved
	 */
	public HexViewPanel (String title,  MainPanel mainPanel, int track, int sector, DiskImage diskImage) throws CbmException {
		this(title, mainPanel, "", diskImage.getBlock(track, sector), DiskImage.BLOCK_SIZE, false, track, sector, diskImage);
	}

	/**
	 * Constructor for file
	 * @param title String with window title
	 * @param mainPanel main panel
	 * @param fileName String with the file name to show.
	 * @param data a byte array with the data to show
	 * @param length the length of data to show
	 * @param readLoadAddr read load address
	 */
	public HexViewPanel (String title, MainPanel mainPanel, final String fileName, final byte[] data, final int length, final boolean readLoadAddr) {
		this(title, mainPanel, fileName, data, length, readLoadAddr, 0, 0, null);
	}

	private HexViewPanel (String title, MainPanel mainPanel, final String fileName, final byte[] data, final int length, final boolean readLoadAddr, int track, int sector, DiskImage diskImage) {
		this.title = title;
		this.data = data;
		this.mainPanel = mainPanel;
		this.diskImage = diskImage;

		setLayout(new BorderLayout());
		if (diskImage != null) {
			add(createTrackSectorPanel(track, sector, diskImage), BorderLayout.NORTH);
			model.setReadOnly(false);
		} else {
			add(new JLabel(fileName), BorderLayout.NORTH);
			model.setReadOnly(true);
		}
		setup(data, length, fileName, readLoadAddr);
	}

	public void showDialog() {
		final var dialog = new JDialog(mainPanel.getParent(), title, true);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		okButton.addActionListener(e -> dialog.dispose());
		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}

	private void setup(byte[] data, int length, String label, boolean readLoadAddr) {

		model.loadData(data, length);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		table.setFont(getTableFont(false));
		table.getTableHeader().setReorderingAllowed(false);

		GuiHelper.keyNavigateTable(table);

		resizeTable(false);

		applyButton.setEnabled(false);
		applyButton.setVisible(!model.isReadOnly());

		table.addPropertyChangeListener(e -> applyButton.setEnabled(model.isDirty()));

		JScrollPane asmScrollPane = new JScrollPane(asmTextArea);
		asmTextArea.setText(Utility.EMPTY);
		asmTextArea.setEditable(false);
		asmTextArea.setFont(getTableFont(false));

		GuiHelper.keyNavigateTextArea(asmTextArea, asmScrollPane);

		// Setup cards
		cards.add(new JScrollPane(table), HEX_MODE);
		cards.add(asmScrollPane, ASM_MODE);
		add(cards, BorderLayout.CENTER);

		add(drawButtons(length, readLoadAddr, label), BorderLayout.SOUTH);
		setSize(table.getWidth(), table.getHeight());
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));

		table.changeSelection(0, 1, false, false);
		table.requestFocus();
	}

	private void tableSelectionAction(DiskImage diskImage) {
		try {
			selectedTrack = -1;
			selectedSector = -1;

			goBlockButton.setText("---:---");
			goBlockButton.setEnabled(false);

			int r = table.getSelectedRow();
			int c = table.getSelectedColumn();
			if (r < diskImage.getFirstTrack() || c >= table.getColumnCount() - 1) {
				return;
			}

			int t = Integer.valueOf((String) table.getValueAt(r, c), 16);
			if (t < diskImage.getFirstTrack() || t > diskImage.getTrackCount()) {
				return;
			}
			if (++c >= table.getColumnCount() - 1) {
				c = 1;
				if (++r >= table.getRowCount()) {
					return;
				}
			}
			int s = Integer.valueOf((String) table.getValueAt(r, c), 16);
			if (s < 0 || s > diskImage.getMaxSectors(t)) {
				return;
			}
			selectedTrack = t;
			selectedSector = s;
			goBlockButton.setText(t + ":" + s);
			goBlockButton.setEnabled(true);

		} catch (NumberFormatException ex) {
			// ignored
		}
	}

	private JPanel createTrackSectorPanel(int track, int sector, final DiskImage diskImage) {

		pos = diskImage.getSectorOffset(track, sector);
		offset.setText(getOffset(track, sector, diskImage));
		offset.setEditable(false);

		trkField = GuiHelper.getNumField(diskImage.getFirstTrack(), diskImage.getTrackCount() + diskImage.getFirstTrack(), track, 3);
		secField = GuiHelper.getNumField(diskImage.getFirstSector(), diskImage.getLastSector(), sector, 3);

		trkField.addActionListener(event -> {
			int trk = (int) trkField.getValue();
			int sec = (int) secField.getValue();
			int maxSect = diskImage.getMaxSectors(trk + diskImage.getFirstTrack() - 1) + diskImage.getFirstSector() - 1;
			if (sec > maxSect) {
				sec = maxSect;
			}
			loadBlock(trk, sec, diskImage);
		});
		secField.addActionListener(event -> loadBlock((int)trkField.getValue(), (int) secField.getValue(), diskImage));

		goBlockButton.setEnabled(false);
		goBlockButton.setMargin(new Insets(1, 4, 1, 4));
		goBlockButton.addActionListener(ev -> loadBlock(selectedTrack, selectedSector, diskImage));
		var downButton = new JButton("-");
		var upButton = new JButton("+");
		downButton.setMargin(new Insets(0,4,0,4));
		upButton.setMargin(new Insets(0,4,0,4));

		downButton.addActionListener(ev ->
			loadBlock(diskImage.getSector(diskImage.getSectorOffset((int)trkField.getValue(), (int) secField.getValue()) - 256), diskImage));
		upButton.addActionListener(ev ->
			loadBlock(diskImage.getSector(diskImage.getSectorOffset((int)trkField.getValue(), (int) secField.getValue()) + 256), diskImage));

		var upDownPanel = new JPanel(new GridLayout(1, 2));
		upDownPanel.add(downButton);
		upDownPanel.add(upButton);

		table.getSelectionModel().addListSelectionListener(ev ->  tableSelectionAction(diskImage));
		table.getColumnModel().addColumnModelListener(new DiskImageTableModelListener(diskImage));

		var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Track:"));
		panel.add(trkField);
		panel.add(new JLabel("Sector:"));
		panel.add(secField);
		panel.add(new JLabel("Offset:"));
		panel.add(offset);
		panel.add(upDownPanel);
		panel.add(goBlockButton);
		return panel;
	}

	private String getOffset(int trk, int sec, DiskImage diskImage) {
		return String.format(" $%06X ", diskImage.getSectorOffset(trk, sec));
	}

	private void loadBlock(TrackSector ts, final DiskImage diskImage) {
		if (ts != null) {
			loadBlock(ts.getTrack(), ts.getSector(), diskImage);
		}
	}

	private void loadBlock(int track, int sector, final DiskImage diskImage) {
		try {
			if (track == -1 || sector == -1) {
				return;
			}
			data = diskImage.getBlock(track, sector);
			table.clearSelection();
			asmTextArea.setText(Utility.EMPTY);
			trkField.setValue(track);
			secField.setValue(sector);
			model.loadData(data, data.length);
			pos = diskImage.getSectorOffset(track, sector);
			offset.setText(getOffset(track, sector, diskImage));
			String code = ProgramParser.parse(data, DiskImage.BLOCK_SIZE, false);
			asmTextArea.setText(code);
			asmTextArea.setCaretPosition(0);
			table.changeSelection(0, 1, false, false);
			table.requestFocus();
		} catch (CbmException e) { /* ignore */ }
	}

	private JPanel drawButtons(final int length, final boolean readLoadAddr, final String fileName) {
		modeButton.setMnemonic('m');
		modeButton.addActionListener(ae -> switchHexAsmMode(length, readLoadAddr));
		c64ModeButton.setMnemonic('c');
		c64ModeButton.addActionListener(ae -> switchFont(c64ModeButton.isSelected()));

		okButton.setMnemonic('o');
		okButton.setToolTipText(Utility.getMessage(Resources.DROID64_HEXVIEW_CLOSE_TOOLTIP));

		var printButton = new JButton(Utility.getMessage(Resources.DROID64_HEXVIEW_PRINT));
		printButton.setMnemonic('p');
		printButton.addActionListener(ae -> print(data, c64Modes[isHexMode() ? 0 : 1], fileName, isHexMode()));

		var saveButton = new JButton(Utility.getMessage(Resources.DROID64_HEXVIEW_SAVETEXT));
		saveButton.setMnemonic('s');
		saveButton.addActionListener(ae-> saveText(data, fileName, isHexMode()));

		var saveDataButton = new JButton(Utility.getMessage(Resources.DROID64_HEXVIEW_SAVEDATA));
		saveDataButton.setMnemonic('d');
		saveDataButton.addActionListener(ae-> saveData(data, fileName));

		applyButton.setMnemonic('a');
		applyButton.addActionListener(ae-> applyData());

		var buttonPanel = new JPanel();
		buttonPanel.add(modeButton);
		buttonPanel.add(c64ModeButton);
		buttonPanel.add(printButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveDataButton);
		buttonPanel.add(applyButton);
		buttonPanel.add(okButton);
		return buttonPanel;
	}

	private void switchFont(boolean useCbmFont) {
		c64ModeButton.setSelected(useCbmFont);
		if (isHexMode()) {
			c64Modes[0] = useCbmFont;
			table.setFont(getTableFont(useCbmFont));
			resizeTable(useCbmFont);
		} else {
			c64Modes[1] = useCbmFont;
			asmTextArea.setFont(getTableFont(useCbmFont));
		}
	}

	private boolean isHexMode() {
		return HEX_MODE.equals(modeButton.getText());
	}

	private void switchHexAsmMode(int length, boolean readLoadAddr) {
		if (isHexMode()) {
			// from hex mode to asm mode
			modeButton.setText(ASM_MODE);
			if (Utility.EMPTY.equals(asmTextArea.getText())) {
				String code = ProgramParser.parse(data, length, readLoadAddr);
				asmTextArea.setText(code);
				asmTextArea.setCaretPosition(0);
			}
			switchFont(c64Modes[1]);
		} else {
			modeButton.setText(HEX_MODE);
			switchFont(c64Modes[0]);
			resizeTable(c64ModeButton.isSelected());
		}

		asmTextArea.setSize(table.getSize());
		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, modeButton.getText());
	}

	private void resizeTable(boolean useCbmFont) {
		int chrWdt = table.getFontMetrics(getTableFont(useCbmFont)).stringWidth("w");
		int colWdt = chrWdt * 3;
		int addrWdt = chrWdt * 10;
		int ascWdt = chrWdt * model.getColumnCount();
		for (int i=0; i<table.getColumnCount(); i++) {
			var column = table.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(addrWdt);
			} else if (i == (model.getColumnCount() - 1)) {
				column.setPreferredWidth(ascWdt);
			} else {
				column.setPreferredWidth(colWdt);
			}
		}
		int wdt = addrWdt + colWdt*(model.getColumnCount() - 2) + ascWdt;
		int hgt=0;
		for (int i=0; i<Math.min(model.getColumnCount() - 2, table.getRowCount()); i++) {
			hgt += table.getRowHeight(i);
		}
		ToolTipManager.sharedInstance().unregisterComponent(table);
		ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
		table.setPreferredScrollableViewportSize(new Dimension(wdt, hgt));
		table.setSize(wdt, hgt);
		table.invalidate();
	}

	private Font getTableFont(boolean useCbmFont) {
		return useCbmFont ? Setting.CBM_FONT.getFont() : Setting.SYS_FONT.getFont();
	}

	private void saveText(byte[] data, String fileName, boolean hexMode) {
		String[] ext = hexMode ? new String[] {".txt", ".asm"} : new String[] {".asm", ".txt"};
		String ftitle = hexMode ? Utility.getMessage(Resources.DROID64_HEXVIEW_SAVEHEX) : Utility.getMessage(Resources.DROID64_HEXVIEW_SAVEASM);
		String fname = FileDialogHelper.openTextFileDialog(ftitle, null, fileName, true, ext);
		if (fname != null) {
			String outString = hexMode ? Utility.hexDump(data) : asmTextArea.getText();
			if (!outString.isEmpty()) {
				writeToFile(new File(fname), outString);
			}
		}
	}

	private void saveData(byte[] data, String fileName) {
		String[] ext = new String[] {".dat", ".bin"};
		String ftitle = Utility.getMessage(Resources.DROID64_HEXVIEW_SAVEDATA);
		String fname = FileDialogHelper.openTextFileDialog(ftitle, null, fileName, true, ext);
		if (fname != null) {
			var file = new File(fname);
			try {
				Utility.writeFile(file, data);
			} catch (CbmException e) {
				mainPanel.appendConsole("Error: failed to write to file "+file.getName()+'\n'+e.getMessage());
			}
		}
	}

	private void applyData() {
		diskImage.save();
		model.setDirty(false);
		applyButton.setEnabled(false);
	}

	private void writeToFile(File saveFile, String outString) {
		try {
			Utility.writeFile(saveFile, outString);
		} catch (CbmException e) {
			mainPanel.appendConsole("Error: failed to write to file "+saveFile.getName()+'\n'+e.getMessage());
		}
	}

	private void print(final byte[] data, boolean useCbmfont, final String title, boolean hexmode) {
		var job = PrinterJob.getPrinterJob();
		if (hexmode) {
			job.setPageable(new PrintPageable(data, useCbmfont, title, mainPanel));
		} else {
			var header = String.format("; Created by %s version %s%n; %s%n%n", DroiD64.PROGNAME, DroiD64.VERSION, new Date().toString());
			if (useCbmfont) {
				header = header.toUpperCase();
			}
			job.setPageable(new PrintPageable(header + asmTextArea.getText(), "; " + title, useCbmfont, true, mainPanel));
		}
		mainPanel.appendConsole("Print " + (hexmode ? "hex" : "asm") + " mode using " + (useCbmfont ? "C64" : "system") + " font");
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException e) {
				mainPanel.appendConsole("Failed to print: " + e);
			}
		}
	}

	private class DiskImageTableModelListener implements TableColumnModelListener {
		private final DiskImage diskImage;

		public DiskImageTableModelListener(DiskImage diskImage) {
			this.diskImage = diskImage;
		}

		@Override
		public void columnAdded(TableColumnModelEvent ev) { /* ignore*/	}

		@Override
		public void columnRemoved(TableColumnModelEvent ev) { /* ignore*/	}

		@Override
		public void columnMoved(TableColumnModelEvent ev) { /* ignore*/ }

		@Override
		public void columnMarginChanged(ChangeEvent ev) { /* ignore*/ }

		@Override
		public void columnSelectionChanged(ListSelectionEvent ev) {
			if (!ev.getValueIsAdjusting()) {
				tableSelectionAction(diskImage);
			}
		}
	}

	/** Class to handle colors of the table cells */
	private static class CustomCellRenderer extends DefaultTableCellRenderer {

		private enum HexColor {
			ADDR_SELECTED(Color.BLACK, new Color(100, 100, 100)),
			ASCII_SELECTED(Color.BLACK, new Color(100, 100, 100)),
			HEX_SELECTED(Color.BLACK, new Color(232, 232, 232)),
			ADDR(Color.BLACK, new Color(200, 200, 200)),
			ASCII(Color.BLACK, new Color(200, 200, 200)),
			HEX(new Color(0, 0, 80), Color.WHITE);

			public final Color fg;
			public final Color bg;

			private HexColor(Color fg, Color bg) {
				this.fg = fg;
				this.bg = bg;
			}
		}

		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			var rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (rendererComp instanceof JLabel) {
				((JLabel)rendererComp).putClientProperty("html.disable", Boolean.TRUE);
			}
			setColor(rendererComp, column, isSelected, table);
			return rendererComp;
		}

		private void setColor(Component comp, int column, boolean isSelected, JTable table) {
			if (column == 0) {
				comp.setForeground(isSelected ? HexColor.ADDR_SELECTED.fg : HexColor.ADDR.fg);
				comp.setBackground(isSelected ? HexColor.ADDR_SELECTED.bg : HexColor.ADDR.bg);
			} else if (column == table.getColumnCount() - 1) {
				comp.setForeground(isSelected ? HexColor.ASCII_SELECTED.fg : HexColor.ASCII.fg);
				comp.setBackground(isSelected ? HexColor.ASCII_SELECTED.bg : HexColor.ASCII.bg);
			} else {
				comp.setForeground(isSelected ? HexColor.HEX_SELECTED.fg : HexColor.HEX.fg);
				comp.setBackground(isSelected ? HexColor.HEX_SELECTED.bg : HexColor.HEX.bg);
			}
		}
	}
}
