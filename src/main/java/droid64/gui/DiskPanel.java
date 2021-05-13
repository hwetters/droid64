package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.BasicParser;
import droid64.d64.CbmException;
import droid64.d64.CbmFile;
import droid64.d64.CpmFile;
import droid64.d64.DirEntry;
import droid64.d64.DiskImage;
import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;
import droid64.d64.ValidationError;
import droid64.db.DaoFactory;
import droid64.db.DatabaseException;

/**<pre style='font-family:sans-serif;'>
 * Created on 23.06.2004<br>
 *
 *   droiD64 - A graphical filemanager for D64 files<br>
 *   Copyright (C) 2004 Wolfram Heyer<br>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   eMail: wolfvoz@users.sourceforge.net <br>
 *   http://droid64.sourceforge.net
 *
 * @author wolf
 * @author henrik
 * </pre>
 */
public class DiskPanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = 1L;

	private static final String PARENT_DIR = "..";
	private static final String ZIP_EXT = ".zip";

	private DiskPanel otherDiskPanel;
	private DiskImage diskImage = null;
	private final EntryTableModel tableModel = new EntryTableModel();
	private final JTable table = new JTable(tableModel);
	private final MainPanel mainPanel;

	/** Disk label from loaded disk image */
	private final JLabel diskLabel = new JLabel(Utility.getMessage(Resources.DROID64_NODISK));
	/** Name (path) of current directory or loaded image */
	private final JComboBox<String> diskName = new JComboBox<>();
	private final List<String> diskNameHistory = new ArrayList<>();
	/** Path to currently used directory on file system */
	private File currentImagePath = null;

	private final JPanel diskLabelPane = new JPanel();

	/** True when a disk image is loaded */
	private boolean imageLoaded = false;

	/** True when a Zip file has been loaded */
	private boolean zipFileLoaded = false;
	private File directory = new File(".");
	private int rowHeight = 12;
	/** True when this is the active disk panel */
	private boolean active = false;
	/** This is the row of the last opened file in the file list */
	private Integer openedRow = null;
	private final transient ConsoleStream consoleStream;


	public DiskPanel(final MainPanel mainPanel, ConsoleStream consoleStream)  {
		this.mainPanel = mainPanel;
		this.consoleStream = consoleStream;

		tableModel.setMode(EntryTableModel.MODE_LOCAL);
		rowHeight = Setting.FONT_SIZE.getInteger() + 2;

		var dirPanel = drawDirPanel();
		setTableColors();
		setLayout(new BorderLayout());
		add(dirPanel, BorderLayout.CENTER);
		setBorder(BorderFactory.createRaisedBevelBorder());
		setupDragDrop(this);
	}

	public void setActive(boolean active) {
		if (active) {
			setBackground(Setting.BORDER_ACTIVE.getColor());
			this.active = true;
			this.table.grabFocus();
			this.table.requestFocus();
			if (otherDiskPanel != null) {
				otherDiskPanel.active = false;
				otherDiskPanel.setBackground(Setting.BORDER_INACTIVE.getColor());
			}
			mainPanel.setButtonState();
		} else {
			setBackground(Setting.BORDER_INACTIVE.getColor());
			this.active = false;
			if (otherDiskPanel != null) {
				otherDiskPanel.active = true;
				otherDiskPanel.setBackground(Setting.BORDER_ACTIVE.getColor());
				otherDiskPanel.table.grabFocus();
				otherDiskPanel.table.requestFocus();
			}
		}
		setTableColors();
		otherDiskPanel.setTableColors();
	}

	public boolean isActive() {
		return this.active;
	}

	private void setupDragDrop(final DiskPanel diskPanel) {
		var handler = new MyTransferHandler();
		setDropTarget(handler.getDropTarget(diskPanel));
		table.setTransferHandler(handler);
		table.setDragEnabled(true);
	}

	private JPanel drawDirPanel() {
		final var columnModel = tableModel.getTableColumnModel();
		tableModel.addTableModelListener(this);

		table.setColumnModel(columnModel);
		table.setDefaultRenderer(Object.class, new ListTableCellRenderer(tableModel));
		table.setGridColor(Setting.DIR_BG.getColor());
		table.setRowHeight(rowHeight);
		table.setBackground(Setting.DIR_BG.getColor());
		table.setForeground(Setting.DIR_FG.getColor());
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyPressed(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyReleased(KeyEvent ev) {
				processFileTableKeyEvent(ev.getKeyCode());
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				setActive(true);
				int row = table.rowAtPoint(me.getPoint());
				if (me.getClickCount() == 2 && row >= 0) {
					doubleClickedRow(row);
				}
			}
		});

		MouseAdapter mousePressedToActivate = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				setActive(true);
			}
		};

		var popMenu = new JPopupMenu();
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_MD5, '5', event -> calcMd5Checksum());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_VIEWBASIC, 'b', event -> basicViewFile());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_VIEWHEX, 'h', event -> hexViewFile());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_VIEWIMAGE, 'i', event -> imageViewFile());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_VIEWTEXT, 't', event -> showFile());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_COPYFILE, 'c', event -> copyFile());
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_RENAMEFILE, 'r', event -> renameFile());

		var pluginMenu = new JMenu("Plugins");
		pluginMenu.setMnemonic('p');
		for ( ExternalProgram ep : Setting.getExternalPrograms()) {
			if (ep != null) {
				GuiHelper.addMenuItem(pluginMenu, ep.getLabel(), null, event ->	doExternalProgram(ep));
			}
		}
		popMenu.add(pluginMenu);
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_DISK_RELOAD, 'r', event -> reloadDiskImage(true));

		table.setComponentPopupMenu(popMenu);

		diskLabel.setBackground(Setting.DIR_BG.getColor());
		diskLabel.setForeground(Setting.DIR_FG.getColor());
		diskLabel.addMouseListener(mousePressedToActivate);

		diskLabelPane.setBackground(Setting.DIR_BG.getColor());
		diskLabelPane.setForeground(Setting.DIR_FG.getColor());
		diskLabelPane.add(diskLabel);
		diskLabelPane.addMouseListener(mousePressedToActivate);

		diskNameHistory.forEach(diskName::addItem);

		diskName.setFont(Setting.SYS_FONT.getFont());
		diskName.setEditable(true);
		diskName.addMouseListener(mousePressedToActivate);
		diskName.addKeyListener(createDiskNameKeyListener());
		diskName.addActionListener(ev -> {
			File selected = new File((String) diskName.getSelectedItem());
			if (selected.isDirectory()) {
				setDiskName(selected.getName());
				loadLocalDirectory(selected);
			}
			if (!active) {
				setActive(true);
			}
		});
		diskName.addItemListener(ae -> {
			if (!active) {
				setActive(true);
			}
		});
		final var backButton = new JButton(MetalIconFactory.getFileChooserUpFolderIcon());
		backButton.addActionListener(ae -> {
			unloadDisk();
			setActive(true);
		});

		Insets bbMargin = backButton.getInsets();
		bbMargin.left = 1;
		bbMargin.right = 1;
		backButton.setMargin(bbMargin);

		var diskNamePanel = new JPanel(new BorderLayout());
		diskNamePanel.add(diskName, BorderLayout.CENTER);
		diskNamePanel.add(backButton, BorderLayout.EAST);

		var scrollPane = new JScrollPane(table);
		scrollPane.addMouseListener(mousePressedToActivate);
		scrollPane.setComponentPopupMenu(popMenu);

		var dirPanel = new JPanel(new BorderLayout());
		dirPanel.add(diskLabelPane, BorderLayout.NORTH);
		dirPanel.add(scrollPane, BorderLayout.CENTER);
		dirPanel.add(diskNamePanel, BorderLayout.SOUTH);
		dirPanel.setPreferredSize(new Dimension(50, 300));
		return dirPanel;
	}

	private void processFileTableKeyEvent(int keyCode) {
		int row = table.getSelectedRow();
		if (keyCode == KeyEvent.VK_TAB) {
			setActive(!isActive());
		} else if (keyCode == KeyEvent.VK_LEFT) {
			if (imageLoaded) {
				unloadDisk();
			} else if (PARENT_DIR.equals(table.getValueAt(0, 1))) {
				doubleClickedLocalfile(currentImagePath.getParentFile(), 0);
			}
		} else if (keyCode == KeyEvent.VK_HOME) {
			table.getSelectionModel().setSelectionInterval(0, 0);
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
		} else if (keyCode == KeyEvent.VK_END) {
			int lastRow = table.getRowCount() - 1;
			table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
			table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
		} else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
			table.clearSelection();
			table.invalidate();
			table.repaint();
		} else if (row >= 0) {
			if (keyCode == KeyEvent.VK_ENTER) {
				doubleClickedRow(row - 1);
			} else if (keyCode == KeyEvent.VK_RIGHT && (row > 0 || !PARENT_DIR.equals(table.getValueAt(0, 1)))) {
				doubleClickedRow(row);
			}
		}
	}

	private KeyListener createDiskNameKeyListener() {
		return new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }
			@Override
			public void keyPressed(KeyEvent ev) {
				if (!active) {
					setActive(true);
				}
			}
			@Override
			public void keyReleased(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
					String selected = (String) diskName.getSelectedItem();
					var dir = new File(selected);
					if (dir.isDirectory()) {
						setDiskName(selected);
						loadLocalDirectory(dir);
					}
				}
			}
		};
	}

	protected String getCurrentPath() {
		return (String) diskName.getSelectedItem();
	}

	private void setDiskName(String name) {
		if (name == null) {
			return;
		}
		ActionListener[] listeners = diskName.getActionListeners();
		for (final ActionListener listener : listeners) {
			diskName.removeActionListener(listener);
		}

		if (!new File(name).isDirectory()) {
			if (diskName.getItemCount() > 0) {
				diskName.setSelectedIndex(0);
				diskName.setSelectedItem(name);
			}
			for (final ActionListener listener : listeners) {
				diskName.addActionListener(listener);
			}
			return;
		}
		if (!diskNameHistory.contains(name)) {
			diskNameHistory.add(name);
			diskName.addItem(name);
		}
		for (int i=0; i<diskName.getItemCount(); i++) {
			if (name.equals(diskName.getItemAt(i))) {
				diskName.setSelectedIndex(i);
				break;
			}
		}
		for (final ActionListener listener : listeners) {
			diskName.addActionListener(listener);
		}
	}

	private String getLocalFilename(int row) {
		return this.currentImagePath + File.separator + table.getValueAt(row, 1);
	}

	protected void doubleClickedRow(int row) {
		if (row < 0) {
			return;
		}
		try {
			openedRow = null;
			if (imageLoaded) {
				doubleClickedImageFileEntry(diskImage.getCbmFile(row), row);
			} else if (zipFileLoaded) {
				String entryName = (String) table.getValueAt(row, 1);
				doubleClickedZipFile(entryName, row);
			} else {
				if (PARENT_DIR.equals(table.getValueAt(row, 1))) {
					doubleClickedLocalfile(currentImagePath.getParentFile(), row);
				} else {
					doubleClickedLocalfile(new File(getLocalFilename(row)), row);
				}
			}
		} catch (Exception e) {	//NOSONAR
			mainPanel.appendConsole("DiskPanel: " + e.getClass().getName() + ": " + e.getMessage());
		}
		mainPanel.setButtonState();
	}

	private void doubleClickedImageFileEntry(CbmFile file, int row) throws CbmException {
		if (file != null && file.getFileType() == FileType.CBM) {
			// Open partition
			clearDirTable();
			diskImage.readPartition(file.getTrack(), file.getSector(), file.getSizeInBlocks());
			showDirectory();

			setDiskName(diskImage.getFile() + File.separator + file.getName());
		} else if (file != null){
			hexViewFile(row);
		}
	}

	protected void doubleClickedZipFile(String zipName, int row) throws CbmException {
		mainPanel.appendConsole("doubleClickedZipFile: "+zipName);

		if (Setting.isImageFileName(new File(zipName))) {
			mainPanel.appendConsole("doubleClickedZipFile: is disk image");
			diskImage = DiskImage.getDiskImage(new File(zipName), getFileData(row), consoleStream);
			diskImage.setFile(new File(zipName));
			setDiskName(currentImagePath + File.separator + zipName);
			clearDirTable();
			imageLoaded = true;
			zipFileLoaded = true;
			updateImageFile();
			showDirectory();
		} else if (PARENT_DIR.equals(zipName)) {
			File file = currentImagePath.getParentFile();
			if (file.exists() && file.isDirectory()) {
				loadLocalDirectory(file);
			}
		} else {
			hexViewFile(row);
		}
		mainPanel.appendConsole("doubleClickedZipFile: done");
	}

	protected void doubleClickedLocalfile(File file, int row) {
		if (file.exists()) {
			if (Setting.isImageFileName(file)) {
				openDiskImage(file, true);
				openedRow = Integer.valueOf(row);
			} else if (file.isDirectory()) {
				loadLocalDirectory(file);
			} else if (file.isFile()) {
				if (file.getName().toLowerCase().endsWith(ZIP_EXT)) {
					loadZipFile(file);
				} else if (row >= 0) {
					hexViewFile(row);
				} else {
					addFile(file);
				}
			} else {
				mainPanel.appendConsole("Can't open "+file);
			}
		} else {
			mainPanel.appendConsole("Can't open "+file);
		}
	}

	private void showNoDiskLoadedMessage() {
		mainPanel.appendConsole("\nNo disk image file selected. Aborting.");
		JOptionPane.showMessageDialog(mainPanel.getParent(),
				Utility.getMessage(Resources.DROID64_INFO_NOIMAGELOADED),
				DroiD64.PROGNAME + " - No disk",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel model = (TableModel) e.getSource();
		table.setModel(model);
		table.revalidate();
	}

	/**
	 * Execute external program (plugin) and write its stdout and stderr to the console.
	 * @param prg external program to be executed.
	 */
	public void doExternalProgram(ExternalProgram prg) {
		var selectedFiles = getSelectedFiles();
		File imgFile = null;
		if (imageLoaded) {
			if (zipFileLoaded) {
				try {
					var tmpfile = File.createTempFile("droid64_", ".img");
					tmpfile.deleteOnExit();
					imgFile = tmpfile;
					diskImage.saveAs(tmpfile);
					mainPanel.appendConsole("Write temporary image to "+imgFile);
				} catch (IOException e) {	//NOSONAR
					GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to create image tempfile from zip file.");
					return;
				}
			} else {
				imgFile = diskImage.getFile();
			}
		} else {
			imgFile = currentImagePath;
		}
		var otherPath = otherDiskPanel.imageLoaded ? otherDiskPanel.diskImage.getFile(): otherDiskPanel.currentImagePath;
		var imageType = imageLoaded ? diskImage.getImageFormat() : DiskImageType.UNDEFINED;
		var execArgs = prg.getExecute(imgFile, selectedFiles, otherPath, directory, imageType);
		File imgParentFile;
		if (imgFile != null) {
			imgParentFile = imgFile.getParentFile();
		} else if (currentImagePath != null) {
			imgParentFile = currentImagePath;
		} else {
			imgParentFile = null;
		}
		prg.runProgram(imgParentFile, execArgs, mainPanel);
	}

	private List<String> getSelectedFiles() {
		var selectedFiles = new ArrayList<String>();
		for (var row : table.getSelectedRows()) {
			final String selectedFile;
			if (imageLoaded) {
				selectedFile = diskImage.getCbmFile(row).getName().toLowerCase();
			} else {
				selectedFile = currentImagePath + File.separator + ((String) tableModel.getValueAt(row, 1));
			}
			selectedFiles.add(selectedFile);
		}
		return selectedFiles;
	}

	public Stream<DirEntry> getDirEntries() {
		return tableModel.getEntries();
	}

	protected int getSelectedRow() {
		return table.getSelectedRow();
	}

	protected void setSelectedRow(int row) {
		if (row > -1 && row < table.getRowCount()) {
			table.setRowSelectionInterval(row, row);
		}
	}

	private void showDirectory() {
		if (imageLoaded) {
			mainPanel.appendConsole("Found " + diskImage.getFilesUsedCount() + " files in this "+DiskImage.getImageTypeName(diskImage.getImageFormat()) + " image file.");
			for (var fileNum = 0; fileNum <= diskImage.getFilesUsedCount() - 1;	fileNum++) {
				if (diskImage.getCbmFile(fileNum) != null) {
					tableModel.updateDirEntry(new DirEntry(diskImage.getCbmFile(fileNum), fileNum + 1));
				} else {
					tableModel.updateDirEntry(null);
				}
			}
			diskLabel.setText(getDiskLabel());
			tableModel.setMode(diskImage.isCpmImage() ? EntryTableModel.MODE_CPM : EntryTableModel.MODE_CBM);
			var tcm = table.getTableHeader().getColumnModel();
			for (var i=0; i< tcm.getColumnCount(); i++) {
				tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
			}
			setTableColors();
			table.setColumnModel(tableModel.getTableColumnModel());
			table.revalidate();
			repaint();
		} else {
			mainPanel.appendConsole("no image loaded");
		}
	}

	private String getDiskLabel() {
		if (imageLoaded) {
			return String.format("%s \"%s,%s\" %d BLOCKS FREE [%d]",
				diskImage.getBam().getDiskDosType(), diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId(), diskImage.getBlocksFree(), diskImage.getFilesUsedCount());
		} else {
			return Utility.EMPTY;
		}
	}

	/**
	 * Load contents from a directory on local file system.
	 * @param path to a directory to open
	 */
	public void loadLocalDirectory(File path) {
		var dir = path != null ? path : Setting.DEFAULT_IMAGE_DIR.getFile();
		if (dir.isDirectory()) {
			mainPanel.appendConsole("loadLocalDirectory: "+dir.getAbsolutePath());
			clearDirTable();
			int fileNum = 0;
			var parentFile = dir.getParentFile();
			if (parentFile != null) {
				DirEntry parent = new DirEntry(parentFile, ++fileNum);
				parent.setName(PARENT_DIR);
				tableModel.updateDirEntry(parent);
			}
			this.currentImagePath = dir;
			var files = dir.listFiles();
			if (files != null) {
				for (var file : Utility.sortFiles(files)) {
					if (!file.getName().startsWith(".")) {
						tableModel.updateDirEntry(new DirEntry(file, ++fileNum));
					}
				}
				setDiskName(dir.getPath());
				diskLabel.setText("FILE SYSTEM");
			} else {
				diskLabel.setText("ERROR");
			}
		} else if (dir.getName().toLowerCase().endsWith(ZIP_EXT)) {
			loadZipFile(dir);
			return;
		} else {
			clearDirTable();
			diskLabel.setText("ERROR");
		}
		tableModel.setMode(EntryTableModel.MODE_LOCAL);
		var tcm = table.getTableHeader().getColumnModel();
		for (int i=0; i< tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
		}
		setTableColors();
		table.setColumnModel(tableModel.getTableColumnModel());
		table.revalidate();
		repaint();
	}

	/**
	 * Load a zip file
	 * @param file the zip file to load
	 */
	private void loadZipFile(File file) {
		mainPanel.appendConsole("loadZipFile: "+file.getName());
		try {
			var list = Utility.getZipFileEntries(file, 2);
			clearDirTable();
			var parent = new DirEntry(file.getParentFile(), 1);
			parent.setName(PARENT_DIR);
			tableModel.updateDirEntry(parent);
			list.forEach(tableModel::updateDirEntry);
			tableModel.setMode(EntryTableModel.MODE_LOCAL);
			var tcm = table.getTableHeader().getColumnModel();
			for (int i=0; i< tcm.getColumnCount(); i++) {
				tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
			}
			setDiskName(file.getAbsolutePath());
			diskLabel.setText("ZIP FILE");
			zipFileLoaded = true;
			currentImagePath = file;
			setTableColors();
			table.setColumnModel(tableModel.getTableColumnModel());
			table.revalidate();
			repaint();
		} catch (CbmException e) {	//NOSONAR
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to open zip file.");
		}
	}

	public void showBAM() {
		if (!imageLoaded){
			showNoDiskLoadedMessage();
			return;
		}
		var bamTab = diskImage.getBamTable();
		if (bamTab != null && bamTab.length > 0) {
			var name = String.format("%s \"%s,%s\"", diskImage.getBam().getDiskDosType(), diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId());
			new BAMPanel(mainPanel).show(name, bamTab, diskImage, isWritableImageLoaded());
			diskImage.readBAM();
		} else {
			mainPanel.appendConsole("No BAM available.");
		}
	}

	private void updateImageFile() {
		if (imageLoaded) {
			diskImage.readBAM();
			diskImage.readDirectory();
			if (Boolean.TRUE.equals(Setting.USE_DB.getBoolean())) {
				var disk = diskImage.getDisk();
				var f = diskImage.getFile();
				var excludePattern = Utility.isEmpty(Setting.EXCLUDED_IMAGE_FILES.getString()) ? null : Pattern.compile(Setting.EXCLUDED_IMAGE_FILES.getString());
				if (excludePattern == null || !excludePattern.matcher(f.getAbsolutePath()).matches()) {
					var p = f.getAbsoluteFile().getParentFile();
					var d = p != null ? p.getAbsolutePath() : null;
					disk.setFilePath(d);
					disk.setFileName(f.getName());
					disk.setImageType(diskImage.getImageFormat());
					disk.setHostName(Utility.getHostName());
					try {
						DaoFactory.getDaoFactory().getDiskDao().save(disk);
					} catch (DatabaseException e) {	//NOSONAR
						GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to update database.");
					}
				}
			}
		} else {
			mainPanel.appendConsole("updateImageFile: no image loaded");
		}
	}

	private void logFileSaveFailedAbort(String filename) {
		mainPanel.appendConsole("Failed to save copy of "+filename+".\nAborting copy.");
	}

	public void copyFile() {
		if (otherDiskPanel.zipFileLoaded) {
			mainPanel.appendConsole("Error: copying to Zip files not supported.");
			return;
		}
		boolean filesCopied = false;
		try {
			boolean success = false;
			if (imageLoaded) {
				for (var row : table.getSelectedRows()) {
					filesCopied = true;
					mainPanel.appendConsole("Disk copy [" + row + "] " + tableModel.getValueAt(row, 2));
					var saveData = diskImage.getFileData(row);
					var source = diskImage.getCbmFile(row);
					CbmFile copy;
					if (source instanceof CpmFile) {
						copy = new CpmFile((CpmFile) source);
					} else {
						copy = new CbmFile(source);
					}
					if (otherDiskPanel.imageLoaded) {
						// Copy file from image to image
						success = otherDiskPanel.diskImage.saveFile(copy, true, saveData);
					} else {
						// Copy file from image to local file system
						var outName = otherDiskPanel.currentImagePath + File.separator + Utility.pcFilename(copy);
						mainPanel.appendConsole("DiskPanel.copyFile: "+outName+" class="+copy.getClass().getName());
						var targetFile = new File(outName);
						success = Utility.writeFileSafe(targetFile, saveData);
					}
					if (!success) {
						logFileSaveFailedAbort(copy.getName());
						break;
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.save();
				}
			} else if (zipFileLoaded) {
				for (var row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						var filename = (String) tableModel.getValueAt(row, 1);
						var data = getFileData(row);
						filesCopied = true;
						mainPanel.appendConsole("Zip copy [" + row + "] " + filename);
						if (otherDiskPanel.imageLoaded) {
							// Copy file from local file system to disk image
							var cbmFile = new CbmFile();
							cbmFile.setName(Utility.cbmFileName(filename, DiskImage.DISK_NAME_LENGTH));
							cbmFile.setFileType(CbmFile.getFileTypeFromFileExtension(filename));
							success = otherDiskPanel.diskImage.saveFile(cbmFile, false, data);
							if (!success) {
								logFileSaveFailedAbort(filename);
								break;
							}
						} else if (otherDiskPanel.zipFileLoaded) {
							mainPanel.appendConsole("Target is a zip file");
						} else {
							// Copy file from file system to file system (no images involved)
							var targetFile = new File(otherDiskPanel.currentImagePath + File.separator + filename);
							success = Utility.writeFileSafe(targetFile, data);
						}
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.save();
				}
			} else {
				// local file system
				for (var row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						var filename = (String) tableModel.getValueAt(row, 1);
						var sourceFile = new File(currentImagePath + File.separator + filename);
						if (sourceFile.isFile()) {
							filesCopied = true;
							mainPanel.appendConsole("Local copy [" + row + "] " + filename);
							if (otherDiskPanel.imageLoaded) {
								// Copy file from local file system to disk image
								var data = Utility.readFile(sourceFile);
								var cbmFile = new CbmFile();
								cbmFile.setName(Utility.cbmFileName(filename, DiskImage.DISK_NAME_LENGTH));
								cbmFile.setFileType(CbmFile.getFileTypeFromFileExtension(filename));
								success = otherDiskPanel.diskImage.saveFile(cbmFile, false, data);
								if (!success) {
									logFileSaveFailedAbort(filename);
									break;
								}
							} else {
								// Copy file from file system to file system (no images involved)
								var targetFile = new File(otherDiskPanel.currentImagePath + File.separator + filename);
								Utility.writeFileSafe(sourceFile, targetFile);
							}
						} else {
							mainPanel.appendConsole("Error: Is not a plain file (" + filename+")");
						}
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.save();
				}
			}
		} catch (Exception|OutOfMemoryError e) {	//NOSONAR
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to copy file.");
		}
		if (filesCopied) {
			otherDiskPanel.reloadDiskImage(true);
		}
	}

	private void addFile(File file) {
		if (imageLoaded) {
			try {
				var data = Utility.readFile(file);
				var cbmFile = new CbmFile();
				cbmFile.setName(Utility.cbmFileName(file.getName(), DiskImage.DISK_NAME_LENGTH));
				cbmFile.setFileType(CbmFile.getFileTypeFromFileExtension(file.getName()));
				boolean success = diskImage.saveFile(cbmFile, false, data);
				if (success) {
					diskImage.save();
				}
				reloadDiskImage(true);
			} catch (Exception e) { //NOSONAR
				GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to add file.");
			}
		} else if (!zipFileLoaded) {
			var targetFile = new File(currentImagePath + File.separator + file.getName());
			mainPanel.appendConsole("Add file on local filsystem.\n"+targetFile.getAbsolutePath());
			Utility.writeFileSafe(file, targetFile);
			loadLocalDirectory(currentImagePath);
		}
	}

	/** Get data from file.
	 * @param file number in listing
	 * @return data
	 */
	private byte[] getFileData(int fileNum) {
		try {
			if (imageLoaded) {
				return diskImage.getFileData(fileNum);
			} else if (zipFileLoaded) {
				return Utility.getDataFromZipFileEntry(currentImagePath, (String) table.getValueAt(fileNum, 1));
			} else {
				return Files.readAllBytes(new File(getLocalFilename(fileNum)).toPath());
			}
		} catch (Exception e) {	//NOSONAR
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to get file data.");
		}
		return new byte[0];
	}

	public void hexViewFile() {
		for (var i : table.getSelectedRows()) {
			mainPanel.appendConsole("Hex view '" + getName(i) + "'");
			hexViewFile(i);
		}
	}

	public void calcMd5Checksum() {
		try {
			for (var i : table.getSelectedRows()) {
				String sum = Utility.calcMd5Checksum(getFileData(i));
				mainPanel.appendConsole(sum + "  " + getName(i));
			}
		} catch (CbmException e) {
			mainPanel.appendConsole("MD5 failed " + e);
		}
	}

	private void hexViewFile(int fileNum) {
		var data = getFileData(fileNum);
		if (data == null || data.length == 0) {
			mainPanel.appendConsole("No data");
			return;
		}
		String name = getName(fileNum);
		new HexViewPanel(DroiD64.PROGNAME+" - Hex view", mainPanel, name, data, data.length, true).showDialog();
	}

	public void basicViewFile() {
		for (var i : table.getSelectedRows()) {
			var name = getName(i);
			mainPanel.appendConsole("BASIC view '" + name + "'");
			var data = getFileData(i);
			var basic = BasicParser.parseCbmBasicPrg(data);
			if (basic != null && !basic.isEmpty()) {
				new TextViewPanel(mainPanel).show(basic, DroiD64.PROGNAME+" - BASIC view", name, Utility.MIMETYPE_TEXT);
			} else {
				mainPanel.appendConsole("Failed to parse BASIC.");
			}
		}
	}

	public void imageViewFile() {
		try {
			var imgList = new ArrayList<byte[]>();
			var imgNameList = new ArrayList<String>();
			for (var i : table.getSelectedRows()) {
				var name = getName(i);
				mainPanel.appendConsole("Image view '" + name + "'");
				var data = getFileData(i);
				if (data.length > 0) {
					imgList.add(data);
					imgNameList.add(name);
				}
			}
			if (!imgList.isEmpty()) {
				new ViewImagePanel(DroiD64.PROGNAME+" - Image view", mainPanel).show(imgList, imgNameList);
			}
		} catch (Exception e) {	//NOSONAR
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to view image.");
		}
	}

	public void showFile() {
		for (var i : table.getSelectedRows()) {
			var name = getName(i);
			mainPanel.appendConsole("View '" + name + "'");
			var data = getFileData(i);
			if (data.length > 0) {
				new TextViewPanel(mainPanel).show(data, DroiD64.PROGNAME, name);
			}
		}
	}

	protected void newDiskImage() {
		mainPanel.appendConsole("New disk image.");
		var result = new RenameDiskImagePanel(mainPanel).showDialog(DroiD64.PROGNAME+" - New disk");
		if (result != null) {
			mainPanel.formatConsole("New Diskname is: \"%s, %s\".", result.getDiskName(), result.getDiskID());
			var defaultName = Setting.checkFileNameExtension(result.getDiskType(), result.isCompressedDisk(), result.getDiskName());
			var imgFile = FileDialogHelper.openImageFileDialog(currentImagePath, defaultName, true);
			if (imgFile == null) {
				return;
			}
			var saveName = Setting.checkFileNameExtension(result.getDiskType(), result.isCompressedDisk(), imgFile.getPath());
			if (saveName != null) {
				createNewDisk(result, new File(saveName));
				reloadDiskImage(true);
			}
		}
	}

	protected void createNewDisk(RenameResult result, File file) {
		mainPanel.formatConsole("Selected file: \"%s\".", file);

		try {
			if (result.isCpmDisk()) {
				switch(result.getDiskType()) {
				case D64:
					diskImage = DiskImageType.D64_CPM_C128.getInstance(consoleStream);
					break;
				case D71:
					diskImage = DiskImageType.D71_CPM.getInstance(consoleStream);
					break;
				case D81:
					diskImage = DiskImageType.D81_CPM.getInstance(consoleStream);
					break;
				case D64_CPM_C64:
				case D64_CPM_C128:
				case D71_CPM:
				case D81_CPM:
					diskImage = result.getDiskType().getInstance(consoleStream);
					break;
				default:
					mainPanel.appendConsole("Filename with unknown file extension. Can't detect CP/M format.\n");
					return;
				}
			} else {
				diskImage = result.getDiskType().getInstance(consoleStream);
			}
		} catch (Exception | OutOfMemoryError e) {
			mainPanel.appendConsole("Failed to create disk image.\n"+e.getMessage()+"\n");
			return;
		}

		diskImage.setCompressed(result.isCompressedDisk());
		diskImage.saveNewImage(file, result.getDiskName(), result.getDiskID());
		diskImage.setFile(file);
		imageLoaded = true;
	}

	public void renameDisk() {
		mainPanel.appendConsole("Rename disk.");
		if (diskImage == null || diskImage.getBam() == null) {
			return;
		}
		var result = new RenameDiskImagePanel(mainPanel).showDialog(DroiD64.PROGNAME+" - Rename disk", diskImage);
		if (result != null) {
			mainPanel.formatConsole("New diskname is: \"%s, %s\".", result.getDiskName(), result.getDiskID());
			diskImage.renameImage(result.getDiskName(), result.getDiskID());
			reloadDiskImage(true);
		}
	}

	public Integer validateDisk() {
		if (diskImage == null) {
			return 0;
		}
		var errors = diskImage.validate(new ArrayList<ValidationError.Error>());
		if (errors != null && errors > 0) {
			Integer warnings = diskImage.getWarnings();
			mainPanel.formatConsole("Validated disk and found %d errors and %d warnings.", errors, warnings);
			new ValidationPanel(mainPanel.getParent()).show(diskImage.getValidationErrorList(), this);
		} else {
			mainPanel.appendConsole("Disk validated without finding any warnings or errors.");
		}
		return errors;
	}

	public void unloadDisk() {
		if (isImageLoaded()) {
			if (diskImage.isPartitionOpen()) {
				mainPanel.appendConsole("Unload partition.");
				diskImage.setCurrentPartition(null);
				reloadDiskImage(true);
				return;
			}
			mainPanel.appendConsole("Unload disk.");
			diskImage = null;
			setDiskName(null);
			imageLoaded = false;
			diskLabel.setText(Utility.getMessage("droid64.nodisk"));
			clearDirTable();
			loadLocalDirectory(currentImagePath);
			table.revalidate();
			if (openedRow != null && openedRow < table.getRowCount() && openedRow >= 0) {
				// Select and scroll to last opened file
				table.getSelectionModel().setSelectionInterval(openedRow, openedRow);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(openedRow, 0, true)));
				openedRow = null;
			}
		} else {
			mainPanel.appendConsole("Load parent.");
			var dirfile = getCurrentImagePath();
			if (dirfile != null && dirfile.getParent() != null) {
				loadLocalDirectory(dirfile.getParentFile());
			}
		}
	}

	public File getCurrentImagePath() {
		return currentImagePath;
	}

	public void renameFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: renaming files in Zip files not supported.");
			return;
		}
		boolean filesRenamed = false;
		for (var i : table.getSelectedRows()) {
			try {
				RenameResult result = null;
				if (imageLoaded) {
					var cbmFile = diskImage.getCbmFile(i);
					var filename = cbmFile.getName();
					mainPanel.appendConsole("RenameFile: \"" + filename + '"');
					result = new RenameFilePanel(mainPanel.getParent()).show(DroiD64.PROGNAME+" - Rename file ", cbmFile);
					if (result != null) {
						filesRenamed = true;
						mainPanel.formatConsole("New filename is: \"%s\" %s.\n[%d] \"%s\"", result.getFileName(), result.getFileType(), cbmFile.getDirPosition(), filename);
						diskImage.renameFile(i, result.getFileName(), result.getFileType());
					}
				} else {
					// local file
					var filename = (String) tableModel.getValueAt(i, 1);
					mainPanel.appendConsole("RenameFile: \"" + filename + '"');
					var oldFile = new File(currentImagePath + File.separator + filename);
					if (oldFile.isFile()) {
						result = new RenameFilePanel(mainPanel.getParent()).show(DroiD64.PROGNAME+" - Rename file ", oldFile);
						if (result != null && !filename.equals(result.getFileName())) {
							var newFile = new File(currentImagePath + File.separator + result.getFileName());
							if (!newFile.exists()) {
								filesRenamed = true;
								mainPanel.appendConsole("New filename is: \"" + result.getFileName() + '"');
								if (!oldFile.renameTo(newFile)) {
									mainPanel.appendConsole("Error: Failed to rename "+newFile.getName());
								}
							} else {
								mainPanel.appendConsole("Error: File "+newFile.getName()+" already exists.");
							}
						}
					}
				}
				if (result == null) {
					break;
				}
			} catch (Exception e) {
				GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to rename file.");
			}
		}
		if (filesRenamed) {
			if (imageLoaded) {
				diskImage.save();
			}
			reloadDiskImage(true);
		}
	}

	public void newFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: add new files to Zip files not supported.");
			return;
		}
		try {
			var result = new RenameFilePanel(mainPanel.getParent()).show(DroiD64.PROGNAME+" - New file ", "", FileType.DEL, imageLoaded, false);
			if (result != null) {
				newFile(result);
				reloadDiskImage(true);
			}
		} catch (Exception | OutOfMemoryError e) {
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to create file.");
		}
	}

	public void mkdir() {
		if (imageLoaded && !diskImage.supportsDirectories()) {
			mainPanel.appendConsole("Error: Makedir not supported.");
			return;
		}

		RenameResult result = null;
		try {
			if (imageLoaded) {
				result = new RenameFilePanel(mainPanel.getParent()).show(DroiD64.PROGNAME+" - New Partition ", "", FileType.CBM, true, true);
			} else {
				result = new RenameFilePanel(mainPanel.getParent()).show(DroiD64.PROGNAME+" - Makedir ", null, FileType.CBM, false, false);
			}
		} catch (CbmException e) {
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Mkdir failed.");
		}
		if (result == null) {
			return;
		}
		mainPanel.appendConsole("mkdir: "+result.getFileName());
		if (imageLoaded) {
			try {
				if (null == diskImage.makedir(result.getFileName(), result.getPartitionSectorCount(), result.getDiskID())) {
					mainPanel.appendConsole("Error: failed to create dir " + result.getFileName() + ".");
				} else {
					diskImage.save();
				}
			} catch (Exception e) {
				GuiHelper.showException(mainPanel.getParent(), "Partition error", e, "Failed creating partition %s.",result.getFileName());
			}
		} else {
			var dir = new File(currentImagePath + File.separator + result.getFileName());
			if (dir.exists()) {
				GuiHelper.showErrorMessage(mainPanel.getParent(), "Makedir failed", "Folder with name %s already exists.",dir);
			} else {
				if (!dir.mkdir()) {
					GuiHelper.showErrorMessage(mainPanel.getParent(), "Makedir error", "Failed to create dir %s.",dir);
				}
			}
		}
		reloadDiskImage(true);
	}

	protected void newFile(RenameResult result) {
		var cbmFile = new CbmFile();
		cbmFile.setName(result.getFileName());
		cbmFile.setFileType(result.getFileType());
		mainPanel.appendConsole("newFile: " + cbmFile.getName());
		diskImage.addDirectoryEntry(cbmFile, 0, 0, false, 0);
		diskImage.save();
	}

	private String getName(int i) {
		return imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
	}

	public void deleteFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: deleting from Zip files not supported.");
			return;
		}
		boolean deletedFiles = false;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				var name = getName(i);
				mainPanel.appendConsole("Delete [" + i + "] " + name);
				try {
					if (imageLoaded) {
						diskImage.deleteFile(diskImage.getCbmFile(i));
					} else {
						Files.delete(new File(name).toPath());
					}
					deletedFiles = true;
				} catch (Exception | OutOfMemoryError e) {
					GuiHelper.showException(mainPanel.getParent(), "Error", e, "Deleting %s failed.", name);
				}
			}
		}
		if (deletedFiles) {
			if (imageLoaded) {
				diskImage.save();
			}
			reloadDiskImage(true);
		}
	}

	private void clearDirTable(){
		tableModel.clear();
		zipFileLoaded = false;
	}

	public void setOtherDiskPanelObject ( DiskPanel otherOne ) {
		otherDiskPanel = otherOne;
	}

	public void openDiskImage(File file, boolean updateList) {
		if (file != null) {
			if (Setting.isImageFileName(file)) {
				try {
					mainPanel.appendConsole("openDiskImage: "+file);
					diskImage = DiskImage.getDiskImage(file, consoleStream);
					imageLoaded = true;
					reloadDiskImage(updateList);
				} catch (Exception | OutOfMemoryError e) {	//NOSONAR
					GuiHelper.showException(mainPanel.getParent(),"Load error", e, "Failed to load disk image.");
					mainPanel.appendConsole("\nError: "+e.getMessage());
					setDiskName(null);
					diskLabel.setText(Utility.getMessage(Resources.DROID64_NODISK));
					imageLoaded = false;
					repaint();
				}
			}
		} else {
			loadLocalDirectory(currentImagePath);
		}
	}

	public void reloadDiskImage(boolean updateList){
		mainPanel.appendConsole("reloadDiskImage "+imageLoaded+'\n');
		if (imageLoaded ){
			try {
				var currentPartition = diskImage.getCurrentPartition();
				diskImage = DiskImage.getDiskImage(diskImage.getFile(), consoleStream);
				setDiskName(diskImage.getFile().getPath());
				clearDirTable();
				updateImageFile();
				if (currentPartition != null) {
					diskImage.setCurrentPartition(currentPartition);
					diskImage.readBAM();
					diskImage.readDirectory();
				}
				if (updateList) {
					showDirectory();
				}
			} catch (Exception | OutOfMemoryError e) {	//NOSONAR
				GuiHelper.showException(mainPanel.getParent(),"Load error", e, "Failed to load disk image.");
				mainPanel.appendConsole("\nError: "+e.getMessage());
				setDiskName(null);
				diskLabel.setText(Utility.getMessage(Resources.DROID64_NODISK));
				imageLoaded = false;
				repaint();
			}
		} else {
			loadLocalDirectory(currentImagePath);
		}
	}

	public void setTableColors() {
		int mode = tableModel.getMode();
		Color colorFg;
		Color colorBg;
		Color colorGrid;
		Font font;
		if (EntryTableModel.MODE_CBM == mode) {
			colorBg = active ? Setting.DIR_BG.getColor() : Setting.DIR_BG.getColor().darker();
			colorFg = active ? Setting.DIR_FG.getColor() : Setting.DIR_FG.getColor().darker();
			colorGrid = active ? Setting.DIR_BG.getColor() : Setting.DIR_BG.getColor().darker();
			table.setRowHeight(Setting.ROW_HEIGHT.getInteger());
			font = Setting.CBM_FONT.getFont();
		} else if (EntryTableModel.MODE_CPM == mode) {
			colorBg = active ? Setting.DIR_CPM_BG.getColor() : Setting.DIR_CPM_BG.getColor().darker();
			colorFg = active ? Setting.DIR_CPM_FG.getColor() : Setting.DIR_CPM_FG.getColor().darker();
			colorGrid = active ? Setting.DIR_CPM_BG.getColor() : Setting.DIR_CPM_BG.getColor().darker();
			table.setRowHeight(Setting.ROW_HEIGHT.getInteger());
			font = Setting.CBM_FONT.getFont();
		} else if (EntryTableModel.MODE_LOCAL == mode) {
			colorBg = active ? Setting.DIR_LOCAL_BG.getColor() : Setting.DIR_LOCAL_BG.getColor().darker();
			colorFg = active ? Setting.DIR_LOCAL_FG.getColor() : Setting.DIR_LOCAL_FG.getColor().darker();
			colorGrid = Setting.DIR_LOCAL_BG.getColor();
			table.setRowHeight(Setting.LOCAL_ROW_HEIGHT.getInteger());
			font = Setting.SYS_FONT.getFont();
		} else {
			mainPanel.appendConsole("DiskPanel.setTableColors: unknown mode " + mode);
			return;
		}
		table.setGridColor(colorGrid);
		table.setBackground(colorBg);
		table.setForeground(colorFg);
		diskLabel.setBackground(colorBg);
		diskLabel.setForeground(colorFg);
		diskLabelPane.setBackground(colorBg);
		diskLabelPane.setForeground(colorFg);
		table.setFont(font);
		diskLabel.setFont(font);
	}

	private ActionListener createDiskImageMenuActionListener() {
		return event -> {
			String cmd = event.getActionCommand();
			if (Resources.DROID64_MENU_DISK_NEW.equals(cmd)) {
				newDiskImage();
			} else if (Resources.DROID64_MENU_DISK_LOAD.equals(cmd)) {
				openedRow = null;
				openDiskImage(FileDialogHelper.openImageFileDialog(currentImagePath, null, false), true);
			} else if (Resources.DROID64_MENU_DISK_SHOWBAM.equals(cmd)) {
				showBAM();
			} else if (Resources.DROID64_MENU_DISK_UNLOAD.equals(cmd)) {
				unloadDisk();
			} else if (Resources.DROID64_MENU_DISK_COPYFILE.equals(cmd)) {
				copyFile();
			} else if (Resources.DROID64_MENU_DISK_RENAMEDISK.equals(cmd)) {
				renameDisk();
			} else if (Resources.DROID64_MENU_DISK_RENAMEFILE.equals(cmd)) {
				renameFile();
			} else if (Resources.DROID64_MENU_DISK_DELETEFILE.equals(cmd)) {
				deleteFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWTEXT.equals(cmd)) {
				showFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWHEX.equals(cmd)) {
				hexViewFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWBASIC.equals(cmd)) {
				basicViewFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWIMAGE.equals(cmd)) {
				imageViewFile();
			} else if (Resources.DROID64_MENU_DISK_PRINTDIR.equals(cmd)) {
				printDir();
			} else if (Resources.DROID64_MENU_DISK_MD5.equals(cmd)) {
				calcMd5Checksum();
			} else if (Resources.DROID64_MENU_DISK_MIRROR.equals(cmd)) {
				openOtherPanel();
			}
		};
	}

	protected void openOtherPanel() {
		if (otherDiskPanel.isImageLoaded() || otherDiskPanel.isZipFileLoaded()) {
			openDiskImage(otherDiskPanel.diskImage.getFile(), true);
		} else  {
			loadLocalDirectory(otherDiskPanel.getCurrentImagePath());
		}
	}

	/**
	 * create a help drag-down menu (just for testing)
	 * @param propertyKey the propertyKey for the menu title
	 * @param mnemonic menu mnemonic
	 * @return the menu
	 */
	public JMenu createDiskImageMenu(String propertyKey, String mnemonic) {
		var menu = new JMenu(Utility.getMessage(propertyKey));
		menu.setMnemonic(mnemonic.charAt(0));
		var listener = createDiskImageMenuActionListener();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_NEW, 'n', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_LOAD, 'l', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_SHOWBAM, 'b', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_UNLOAD, 'u', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_COPYFILE, 'c', listener);
		menu.addSeparator();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_RENAMEDISK, 'r', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_RENAMEFILE, 'f', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_DELETEFILE, 'd', listener);
		menu.addSeparator();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWTEXT,  't', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWHEX,   'h', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWBASIC, 'a', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWIMAGE, 'g', listener);
		menu.addSeparator();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_PRINTDIR, 'p', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_MD5, '5', listener);
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_DISK_MIRROR, 'm', listener);
		return menu;
	}

	private void printDir() {
		String title;
		var lines = new ArrayList<String>();
		if (imageLoaded) {
			title = diskImage.getFile().getName();
			var label = String.format("0 \"%-16s\" %-5s", diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId());
			mainPanel.appendConsole("Print image "+ diskImage.getFile());
			lines.add(label);
			for (var i=0; i < diskImage.getFilesUsedCount(); i++) {
				CbmFile file = diskImage.getCbmFile(i);
				if (file instanceof CpmFile) {
					lines.add(((CpmFile)file).asDirString());
				} else {
					lines.add(file.asDirString());
				}
			}
		} else if (zipFileLoaded) {
			title = currentImagePath.getName();
			mainPanel.appendConsole("Print zip file " + currentImagePath);
			int i = 0;
			DirEntry entry;
			do {
				entry = tableModel.getDirEntry(i++);
				if (entry != null && !PARENT_DIR.equals(entry.getName())) {
					String row = String.format("%8d %-20s\t %s", entry.getBlocks(), entry.getName(), entry.getFlags());
					lines.add(row);
				}
			} while (entry != null);
		} else {
			title = directory.getName();
			mainPanel.appendConsole("Print directory "+ directory);
			int i = 0;
			DirEntry entry;
			do {
				entry = tableModel.getDirEntry(i++);
				if (entry != null) {
					String row = String.format("%s\t %8d\t %s", entry.getFlags(), entry.getBlocks(), entry.getName());
					lines.add(row);
				}
			} while (entry != null);
		}
		try {
			var job = PrinterJob.getPrinterJob();
			job.setPageable(new PrintPageable(lines.toArray(new String[lines.size()]), title, imageLoaded, true, mainPanel));
			if (job.printDialog()) {
				job.print();
			}
		} catch (Exception | OutOfMemoryError e) { // NOSONAR
			GuiHelper.showException(mainPanel.getParent(), "Error", e, "Failed to print.");
		}
	}

	/**
	 * @return number of rows
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param i height of rows
	 */
	public void setRowHeight(int i) {
		rowHeight = i;
		table.setRowHeight(rowHeight);
		table.revalidate();
	}

	public void setDirectory(File dir) {
		this.directory = dir;
	}

	public boolean isImageLoaded() {
		return imageLoaded;
	}

	public boolean isWritableImageLoaded() {
		if (imageLoaded && !zipFileLoaded && !diskImage.isCpmImage()) {
			switch (diskImage.getImageFormat()) {
			case D64:
			case D67:
			case D71:
			case D80:
			case D81:
			case D82:
			case D88:
			case T64:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isZipFileLoaded() {
		return zipFileLoaded;
	}

	public File getDirectory() {
		return directory;
	}

	public void moveFile(final boolean upwards) {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: files can't be moved in zip file.");
			return;
		} else if (!imageLoaded) {
			mainPanel.appendConsole(Utility.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
			return;
		}
		boolean save = false;
		int newPos = -1;
		for (var i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				if ((upwards && i==0) || (!upwards && i == table.getRowCount() - 1)) {
					// Can't be moved any more. Is already first or last.
					continue;
				}
				if (upwards) {
					for (var j=i - 1; j >= 0; j--) {
						if (!diskImage.getCbmFile(j).isFileScratched()) {
							newPos = j;
							break;
						}
					}
				} else {
					for (var j=i + 1; j < diskImage.getFilesUsedCount(); j++) {
						if (!diskImage.getCbmFile(j).isFileScratched()) {
							newPos = j;
							break;
						}
					}
				}
				if (newPos >= 0) {
					var cbmFile1 = new CbmFile(diskImage.getCbmFile(i));
					var cbmFile2 = new CbmFile(diskImage.getCbmFile(newPos));
					diskImage.switchFileLocations(cbmFile1, cbmFile2);
					save = true;
					diskImage.readDirectory();
				}
			}
		}
		if (save) {
			diskImage.save();
		}
		reloadDiskImage(true);
		if (save && newPos >= 0) {
			table.setRowSelectionInterval(newPos, newPos);
		}
	}

	public void sortFiles() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: files can't be sorted in zip file.");
			return;
		} else if (!imageLoaded) {
			mainPanel.appendConsole(Utility.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
			return;
		}
		int rowCount =  table.getRowCount();
		boolean swapped = true;
		for (var i = 0; i < (rowCount - 1) && swapped; i++) {
			swapped = false;
			for (int j = 0; j < (rowCount - i - 1); j++) {
				var cbmFile1 = diskImage.getCbmFile(j);
				var cbmFile2 = diskImage.getCbmFile(j + 1);
				if (cbmFile1.compareTo(cbmFile2) > 0) {
					diskImage.setCbmFile(j, cbmFile2);
					diskImage.setCbmFile(j + 1, cbmFile1);
					diskImage.switchFileLocations(cbmFile1, cbmFile2);
					swapped = true;
					diskImage.readDirectory();
				}
			}
		}
		diskImage.save();
		reloadDiskImage(true);
	}

	public List<ValidationError> repairValidationErrors(List<ValidationError.Error> repairList) {
		if (!imageLoaded) {
			return new ArrayList<>();
		}
		diskImage.validate(repairList);
		diskImage.save();
		reloadDiskImage(true);
		return diskImage.getValidationErrorList();
	}

	public DiskImageType getDiskImageType() {
		return imageLoaded ? diskImage.getDiskImageType() : DiskImageType.UNDEFINED;
	}

	public boolean supportsDirectories() {
		return !imageLoaded || diskImage.supportsDirectories();
	}

	public int getImageLength() {
		return diskImage!=null ? diskImage.size() : -1;
	}

	/** Darg and drop handler class */
	private class MyTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;

		@Override
		public int getSourceActions(JComponent c) {
			return DnDConstants.ACTION_COPY;
		}

		public DropTarget getDropTarget(DiskPanel diskPanel) {
			return new DropTarget() {
				private static final long serialVersionUID = 1L;
				@Override
				public synchronized void drop(DropTargetDropEvent event) {
					try {
						handleDropEvent(diskPanel, event);
					} catch (Exception e) {	//NOSONAR
						mainPanel.appendConsole("Failed to open dropped item.\n"+e.getMessage()+'\n');
					}
				}
			};
		}

		@Override
		public Transferable createTransferable(JComponent comp) {
			var dropTable = (JTable) comp;
			if (!imageLoaded) {
				int row = dropTable.getSelectedRow();
				var value = getLocalFilename(row);
				if (new File(value).exists()) {
					return new StringSelection(value);
				}
				return null;
			} else {
				return new StringSelection(getSelectedAsString(dropTable));
			}
		}

		@Override
		public boolean canImport(TransferHandler.TransferSupport info) {
			return true;
		}

		private String getSelectedAsString(JTable table) {
			var b = new StringBuilder();
			if (imageLoaded) {
				b.append(String.format("%s \"%-16s\" %s%n",
						diskImage.getBam().getDiskDosType(), diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId()));
			}
			for (int row = 0; row < table.getRowCount(); row++) {
				if (table.isRowSelected(row)) {
					if (imageLoaded) {
						b.append(diskImage.getCbmFile(row).asDirString()).append('\n');
					} else {
						for (int c=0; c<table.getColumnCount(); c++) {
							if (c > 0) {
								b.append('\t');
							}
							b.append(table.getValueAt(row, c));
						}
						b.append('\n');
					}
				}
			}
			if (imageLoaded) {
				b.append(diskImage.getBlocksFree()).append(" BLOCKS FREE.\n");
			}
			return b.toString();
		}

		private void handleDropEvent(final DiskPanel diskPanel, DropTargetDropEvent event) throws UnsupportedFlavorException, IOException {
			var trans = event.getTransferable();
			if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				mainPanel.appendConsole("Dropped file list: \n");
				event.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				var fileList = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);
				for (var file : fileList) {
					mainPanel.appendConsole("Dropped file "+file+'\n');
					doubleClickedLocalfile(file, -1);
					diskPanel.setActive(true);
				}
			} else if (trans.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				mainPanel.appendConsole("Dropped image\n");
				event.acceptDrop(DnDConstants.ACTION_COPY);
				var img = (Image) trans.getTransferData(DataFlavor.imageFlavor);
				new ViewImagePanel("Image", mainPanel).show(img, "image");
			} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				var s = (String) trans.getTransferData(DataFlavor.stringFlavor);
				doubleClickedLocalfile(new File(s), -1);
				diskPanel.setActive(true);
				mainPanel.appendConsole("Dropped string: "+s+"\n");
				new TextViewPanel(mainPanel).show(s, "string", "string", "text/text");
			} else {
				mainPanel.appendConsole("Unsupported drop type.\n");
			}
		}
	}

}
