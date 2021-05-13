package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.DiskImageType;
import droid64.d64.Utility;
import droid64.db.Bookmark;
import droid64.db.Bookmark.BookmarkType;
import droid64.db.DaoFactory;
import droid64.db.DaoFactoryImpl;
import droid64.db.DatabaseException;
import droid64.db.DiskList;

/**<pre>
 * Created on 21.06.2004
 *
 *   droiD64 - A graphical filemanager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
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
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 * @author wolf
 *</pre>
 */
public class MainPanel implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int COLOUR_POWER_1 = 5;
	private static final int COLOUR_POWER_2 = 35;

	public static final Insets BUTTON_MARGINS = new Insets(1, 4, 1, 4);
	/** Left disk panel */
	private final DiskPanel diskPanel1;
	/** Right disk panel */
	private final DiskPanel diskPanel2;
	/** True when scanning for disk images is running. */
	private boolean scannerActive = false;
	private final transient HashMap<Object, Object> colorHashMap = new HashMap<>();
	/** The position of the divider in the splitPane. */
	private int dividerLoc = -1;
	/** The size of the divider in the splitPane. */
	private int dividerSize = 10;
	/** Buttons are put into GUI in numerical order, left to right, and then next row. */
	enum Button {
		LOAD_DISK_BUTTON,	BAM_BUTTON,			UP_BUTTON,				COPY_FILE_BUTTON,	MD5_BUTTON ,
		VIEW_TEXT_BUTTON,	PLUGIN_1_BUTTON,	HIDE_CONSOLE_BUTTON,	UNLOAD_DISK_BUTTON,	VALIDATE_DISK_BUTTON,
		DOWN_BUTTON,		NEW_FILE_BUTTON,	MKDIR_BUTTON, VIEW_IMAGE_BUTTON,	PLUGIN_2_BUTTON,	SEARCH_BUTTON,
		NEW_DISK_BUTTON, RENAME_DISK_BUTTON,  SORT_FILES_BUTTON , RENAME_FILE_BUTTON ,	DELETE_FILE_BUTTON,
		VIEW_HEX_BUTTON,	PLUGIN_3_BUTTON,	 SETTINGS_BUTTON,
		MIRROR_BUTTON ,	PLUGIN_5_BUTTON,	PLUGIN_6_BUTTON,	PLUGIN_7_BUTTON,
		PLUGIN_8_BUTTON, VIEW_BASIC_BUTTON , PLUGIN_4_BUTTON,  EXIT_BUTTON
	}
	/** Number of columns of buttons. */
	private static final int NUM_BUTTON_COLUMNS = 8;
	/** Number of rows of buttons. */
	private static final int NUM_BUTTON_ROWS = 4;
	/** Array with the button ID for each plugin button */
	private static final Button[] PLUGIN_IDS = { Button.PLUGIN_1_BUTTON, Button.PLUGIN_2_BUTTON, Button.PLUGIN_3_BUTTON, Button.PLUGIN_4_BUTTON,
			Button.PLUGIN_5_BUTTON, Button.PLUGIN_6_BUTTON, Button.PLUGIN_7_BUTTON, Button.PLUGIN_8_BUTTON };
	/** Map containing all buttons */
	private final Map<Button,JComponent> buttonMap = new TreeMap<>();

	private static final String[] LOOK_AND_FEEL_CLASSES = {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
			"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
			"com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
	};

	// Labels
	private static final String LBL_NODISK = "noDisk";
	private static final String LBL_INSERTERROR = "insertError";

	// BUTTONS
	private JButton unloadDiskButton ;
	private JButton showBamButton;
	private JButton validateDiskButton;
	private JButton renameDiskButton;
	private JButton upButton;
	private JButton downButton;
	private JButton sortButton;
	private JButton copyButton;
	private JButton newFileButton;
	private JButton mkdirButton;
	private JButton delPRGButton;
	private JButton renamePRGButton;

	private JButton md5Button;
	private JButton searchButton;

	/** The plugin buttons. Used to be able to change the label from settings. */
	private final JButton[] pluginButtons = new JButton[Setting.MAX_PLUGINS];
	private JToggleButton consoleHideButton;
	private final JFrame parent;
	/** The menu shown when database is used */
	private final JMenu searchMenu = new JMenu(Utility.getMessage(Resources.DROID64_MENU_SEARCH));
	/** The console */
	private final JTextArea consoleText = new JTextArea();
	private final transient ConsoleStream consoleStream = new ConsoleStream(consoleText);
	private final transient OutputStreamWriter console = new OutputStreamWriter(consoleStream);
	/** The split pane used for the console at the bottom and the rest in the upper half. */
	private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	private static String releaseNotes = null;
	private static String manual = null;
	private JMenu bookmarkMenu = null;
	private JToolBar bookmarkBar = new JToolBar();

	/**
	 * Constructor.
	 * @param parent parent frame
	 */
	public MainPanel(JFrame parent) {
		this.parent = parent;
		FileDialogHelper.setOwner(parent);
		parent.setTitle(DroiD64.PROGNAME+" v"+DroiD64.VERSION + " - " + DroiD64.TITLE );

		doSettings(parent);
		diskPanel1 = new DiskPanel(this, consoleStream);
		diskPanel2 = new DiskPanel(this, consoleStream);
		drawPanel(parent);
		diskPanel1.setDirectory(Setting.DEFAULT_IMAGE_DIR.getFile());
		diskPanel2.setDirectory(Setting.DEFAULT_IMAGE_DIR2.getFile());
		diskPanel1.setOtherDiskPanelObject(diskPanel2);
		diskPanel2.setOtherDiskPanelObject(diskPanel1);
		diskPanel1.setActive(true);
		// Setup GUI
		setupMenuBar(parent);
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		parent.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						exitThisProgram();
					}
				});
		saveDefaultValues();
		doSettings(parent);
		setMainWindowSize(parent);
		diskPanel1.loadLocalDirectory(Setting.DEFAULT_IMAGE_DIR.getFile());
		diskPanel2.loadLocalDirectory(Setting.DEFAULT_IMAGE_DIR2.getFile());

		hideConsole(Setting.HIDECONSOLE.getBoolean());
	}

	/**
	 * Setup menu
	 * @param parent parent frame
	 */
	public void setupMenuBar(final JFrame parent) {
		var menubar = new JMenuBar();
		menubar.add(createProgramMenu(parent));
		menubar.add(diskPanel1.createDiskImageMenu(Resources.DROID64_MENU_DISK_1, "1"));
		menubar.add(diskPanel2.createDiskImageMenu(Resources.DROID64_MENU_DISK_2, "2"));
		menubar.add(createBookmarkMenu());
		menubar.add(createSearchMenu(parent));
		menubar.add(createHelpMenu(parent));
		parent.setJMenuBar(menubar);
		menubar.revalidate();
		menubar.repaint();
	}

	protected DiskPanel getActiveDiskPanel() {
		if (diskPanel1 != null && diskPanel1.isActive()) {
			return diskPanel1;
		} else if (diskPanel2 != null && diskPanel2.isActive()) {
			return diskPanel2;
		} else {
			return null;
		}
	}

	private DiskPanel getInactiveDiskPanel() {
		if (diskPanel1 != null && diskPanel1.isActive()) {
			return diskPanel2;
		} else if (diskPanel2 != null && diskPanel2.isActive()) {
			return diskPanel1;
		} else {
			return null;
		}
	}

	/**
	 * Setup main panel
	 */
	private void drawPanel(final JFrame parent) {
		var dirListPanel = new JPanel(new GridLayout(1,2));
		dirListPanel.add(diskPanel1);
		dirListPanel.add(diskPanel2);
		// Create all buttons into the buttonMap
		createDiskOperationButtons(parent);
		createFileOperationButtons(parent);
		createViewFileButtons();
		createOtherButtons(parent);
		// Put buttons in GUI
		var buttonPanel = new JPanel(new GridLayout(NUM_BUTTON_ROWS, NUM_BUTTON_COLUMNS));
		buttonMap.entrySet().forEach(entry -> buttonPanel.add(entry.getValue()));

		var listButtonPanel = new JPanel(new BorderLayout());
        listButtonPanel.add(dirListPanel, BorderLayout.CENTER);
		if (Boolean.TRUE.equals(Setting.BOOKMARK_BAR.getBoolean())) {
			listButtonPanel.add(bookmarkBar, BorderLayout.NORTH);
		}

		listButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
		splitPane.setContinuousLayout(true);
		splitPane.setTopComponent(listButtonPanel);
		splitPane.setBottomComponent(createConsolePanel());
		splitPane.setDividerLocation(0.80);
		splitPane.setResizeWeight(0.80);
		parent.setContentPane(splitPane);
	}

	private Color adjustedColor(Color color, int red, int green, int blue) {
		int newRed = Math.min(Math.max(0, color.getRed() + red), 255);
		int newGreen = Math.min(Math.max(0, color.getGreen() + green), 255);
		int newBlue = Math.min(Math.max(0, color.getBlue() + blue), 255);
		return new Color(newRed, newGreen, newBlue);
	}

	private JToggleButton createToggleButton(String propertyKey, char mnemonic, Button buttonKey, ActionListener listener) {
		var button = new JToggleButton(Utility.getMessage(propertyKey + ".label"));
		button.setMnemonic(mnemonic);
		button.setToolTipText(Utility.getMessage(propertyKey + ".tooltip"));
		button.setMargin(BUTTON_MARGINS);
		button.addActionListener(listener);
		buttonMap.put(buttonKey, button);
		setButtonColor(button, buttonKey);
		return button;
	}

	private JButton createButton(String label, char mnemonic, Button buttonKey, String toolTip, ActionListener listener) {
		var button = new JButton(label);
		button.setMnemonic(mnemonic);
		button.setToolTipText(toolTip);
		button.setMargin(BUTTON_MARGINS);
		button.addActionListener(listener);
		setButtonColor(button, buttonKey);
		buttonMap.put(buttonKey, button);
		return button;
	}

	private JButton createButton(String propertyKey, char mnemonic, Button buttonKey, ActionListener listener) {
		var button = new JButton(Utility.getMessage(propertyKey + ".label"));
		button.setMnemonic(mnemonic);
		button.setToolTipText(Utility.getMessage(propertyKey + ".tooltip"));
		button.setMargin(BUTTON_MARGINS);
		button.addActionListener(listener);
		button.setActionCommand(propertyKey);
		setButtonColor(button, buttonKey);
		buttonMap.put(buttonKey, button);
		return button;
	}

	private void setButtonColor(JComponent button, Button buttonKey) {
		switch (buttonKey) {
		case LOAD_DISK_BUTTON:
		case UNLOAD_DISK_BUTTON:
		case NEW_DISK_BUTTON:
		case BAM_BUTTON:
		case VALIDATE_DISK_BUTTON:
		case RENAME_DISK_BUTTON:
		case MIRROR_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), -20, -20, 20));
			break;
		case UP_BUTTON:
		case DOWN_BUTTON:
		case SORT_FILES_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), -20, 20, -20));
			break;
		case COPY_FILE_BUTTON:
		case NEW_FILE_BUTTON:
		case DELETE_FILE_BUTTON:
		case RENAME_FILE_BUTTON:
		case MKDIR_BUTTON:
		case MD5_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), 20, -20, 20));
			break;
		case VIEW_IMAGE_BUTTON:
		case VIEW_HEX_BUTTON:
		case VIEW_TEXT_BUTTON:
		case VIEW_BASIC_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), 20, 20, -20));
			break;
		case PLUGIN_1_BUTTON:
		case PLUGIN_2_BUTTON:
		case PLUGIN_3_BUTTON:
		case PLUGIN_4_BUTTON:
		case PLUGIN_5_BUTTON:
		case PLUGIN_6_BUTTON:
		case PLUGIN_7_BUTTON:
		case PLUGIN_8_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), -20, 20, 20));
			break;
		case HIDE_CONSOLE_BUTTON:
		case SETTINGS_BUTTON:
		case EXIT_BUTTON:
		case SEARCH_BUTTON:
			button.setBackground(adjustedColor(button.getBackground(), 30, -20, -20));
			break;
		default:
			break;
		}
	}

	private void unloadDiskImage() {
		var diskPanel = getActiveDiskPanel();
		if  (diskPanel != null) {
			diskPanel.unloadDisk();
			setButtonState();
		}
	}

	private void enableButtons(boolean enabled, JButton... buttons) {
		for (var button : buttons) {
			button.setEnabled(enabled);
		}
	}

	public void setButtonState() {
		var activePanel = getActiveDiskPanel();
		var inactivePanel = getInactiveDiskPanel();
		showBamButton.setEnabled(activePanel != null && activePanel.isImageLoaded());
		unloadDiskButton.setText(activePanel != null && activePanel.isImageLoaded() ? "Unload" : "Parent");
		enableButtons(activePanel!= null && activePanel.supportsDirectories(), mkdirButton);
		if (activePanel != null && activePanel.isWritableImageLoaded()) {
			enableButtons(!activePanel.isZipFileLoaded(), validateDiskButton, renameDiskButton, upButton, downButton, sortButton);
			boolean enabled = inactivePanel != null && (inactivePanel.isWritableImageLoaded() || (!inactivePanel.isImageLoaded() && !inactivePanel.isZipFileLoaded()));
			enableButtons(enabled, copyButton, newFileButton, delPRGButton, renamePRGButton);
		} else {
			enableButtons(false, newFileButton, validateDiskButton, renameDiskButton, upButton, downButton, sortButton);
			boolean enabled = inactivePanel != null && (inactivePanel.isWritableImageLoaded() || (!inactivePanel.isImageLoaded() && !inactivePanel.isZipFileLoaded()));
			enableButtons(enabled, copyButton, delPRGButton, renamePRGButton);
		}
	}

	private void createOtherButtons(final JFrame parent) {
		ActionListener pluginButtonListener = event -> {
			var list = Setting.getExternalPrograms();
			for (int cnt = 0; cnt < list.size(); cnt ++){
				if (event.getSource() == pluginButtons[cnt] ){
					var diskPanel = getActiveDiskPanel();
					var prg = list.get(cnt);
					if (diskPanel != null && prg != null) {
						diskPanel.doExternalProgram(prg);
					}
				}
			}
		};
		var externalPrograms = Setting.getExternalPrograms();
		for (var i = 0; i < pluginButtons.length && i < externalPrograms.size(); i++) {
			var ep = externalPrograms.get(i);
			var label = ep != null ? ep.getLabel() : null;
			var tooltip = ep != null ? ep.getDescription() : null;
			pluginButtons[i] = createButton(label != null ? label : "", Integer.toString(i+1).charAt(0), PLUGIN_IDS[i], tooltip, pluginButtonListener);
		}
		consoleHideButton = createToggleButton(Resources.DROID64_BUTTON_HIDECONSOLE, 'e', Button.HIDE_CONSOLE_BUTTON,
				ae-> hideConsole(consoleHideButton.isSelected()));
		consoleHideButton.setSelected(Setting.HIDECONSOLE.getBoolean());
		createButton(Resources.DROID64_BUTTON_SETTINGS, 'S', Button.SETTINGS_BUTTON, ae -> showSettings(parent));
		createButton(Resources.DROID64_BUTTON_EXIT, 'x', Button.EXIT_BUTTON, ae -> exitThisProgram());
	}

	private void hideConsole(boolean hide) {
		splitPane.getBottomComponent().setVisible(!hide);
		if (!hide) {
			splitPane.setDividerLocation(dividerLoc);
			splitPane.setDividerSize(dividerSize);
		} else {
			dividerLoc = splitPane.getDividerLocation();
			dividerSize = splitPane.getDividerSize();
			splitPane.setDividerSize(0);
		}
	}

	private void createDiskOperationButtons(final JFrame parent) {
		createButton(Resources.DROID64_BUTTON_NEWDISK, 'n', Button.NEW_DISK_BUTTON, ae -> {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null) {
				diskPanel.newDiskImage();
			}
		});
		createButton(Resources.DROID64_BUTTON_LOADDISK, 'l', Button.LOAD_DISK_BUTTON, ae -> {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null) {
				File imgFile = FileDialogHelper.openImageFileDialog(diskPanel.getDirectory(), null, false);
				diskPanel.openDiskImage(imgFile, true);
			}
		});
		unloadDiskButton = createButton(Resources.DROID64_BUTTON_UNLOADDISK, 'u', Button.UNLOAD_DISK_BUTTON, ae -> unloadDiskImage());
		showBamButton = createButton(Resources.DROID64_BUTTON_SHOWBAM, 'b', Button.BAM_BUTTON, ae ->  {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null && diskPanel.isImageLoaded()) {
				diskPanel.showBAM();
			} else {
				showErrorMessage(parent, LBL_NODISK);
			}
		});
		renameDiskButton = createButton(Resources.DROID64_BUTTON_RENAMEDISK, 'r', Button.RENAME_DISK_BUTTON, ae -> {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null && diskPanel.isImageLoaded()) {
				diskPanel.renameDisk();
			} else {
				showErrorMessage(parent, LBL_NODISK);
				return;
			}
		});
		validateDiskButton = createButton(Resources.DROID64_BUTTON_VALIDATE, 'v', Button.VALIDATE_DISK_BUTTON, ae -> {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null && diskPanel.isImageLoaded()) {
				diskPanel.validateDisk();
			} else {
				showErrorMessage(parent, LBL_NODISK);
				return;
			}
		});
		createButton(Resources.DROID64_BUTTON_MIRROR, 'v', Button.MIRROR_BUTTON, ae -> {
			var diskPanel = getActiveDiskPanel();
			if (diskPanel != null) {
				diskPanel.openOtherPanel();
			}
		});
	}

	private void createViewFileButtons() {
		var viewTextButton = createButton(Resources.DROID64_BUTTON_VIEWTEXT, 't', Button.VIEW_TEXT_BUTTON, null);
		var hexViewButton = createButton(Resources.DROID64_BUTTON_VIEWHEX, 'h', Button.VIEW_HEX_BUTTON, null);
		var basicViewButton = createButton(Resources.DROID64_BUTTON_VIEWBASIC, 's', Button.VIEW_BASIC_BUTTON, null);
		var imageViewButton = createButton(Resources.DROID64_BUTTON_VIEWIMAGE, 'm', Button.VIEW_IMAGE_BUTTON, null);
		ActionListener viewListener = event -> {
			try {
			var diskPanel = getActiveDiskPanel();
			var cmd = event.getActionCommand();
			if (diskPanel == null || cmd == null) {
				return;
			} else if (viewTextButton.getActionCommand().equals(cmd)) {
				diskPanel.showFile();
			} else if (hexViewButton.getActionCommand().equals(cmd)) {
				diskPanel.hexViewFile();
			} else if (basicViewButton.getActionCommand().equals(cmd)) {
				diskPanel.basicViewFile();
			} else if (imageViewButton.getActionCommand().equals(cmd)) {
				diskPanel.imageViewFile();
			}
			} catch (Exception |OutOfMemoryError ex) {
				GuiHelper.showException(getParent(), "Error", ex, "Failed to open file.");
			}
		};
		viewTextButton.addActionListener(viewListener);
		hexViewButton.addActionListener(viewListener);
		basicViewButton.addActionListener(viewListener);
		imageViewButton.addActionListener(viewListener);
	}

	private void createFileOperationButtons(final JFrame parent) {
		upButton = createButton(Resources.DROID64_BUTTON_UP, 'U', Button.UP_BUTTON, event -> {
			var diskPanel = getActiveDiskPanel();
			if (upButton.getActionCommand().equals(event.getActionCommand())) {
				if (diskPanel != null && diskPanel.isImageLoaded()) {
					diskPanel.moveFile(true);
				} else {
					appendConsole(Utility.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
				}
			}
		});
		downButton = createButton(Resources.DROID64_BUTTON_DOWN, 'D', Button.DOWN_BUTTON, event -> {
			var diskPanel = getActiveDiskPanel();
			if (downButton.getActionCommand().equals(event.getActionCommand())) {
				if (diskPanel != null && diskPanel.isImageLoaded()) {
					diskPanel.moveFile(false);
				} else {
					appendConsole(Utility.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
				}
			}
		});
		sortButton = createButton(Resources.DROID64_BUTTON_SORT, 'S', Button.SORT_FILES_BUTTON, event -> {
			if (sortButton.getActionCommand().equals(event.getActionCommand())) {
				var diskPanel = getActiveDiskPanel();
				if (diskPanel != null) {
					diskPanel.sortFiles();
				}
			}
		});
		copyButton = createButton(Resources.DROID64_BUTTON_COPY, 'c', Button.COPY_FILE_BUTTON, event -> {
			if (copyButton.getActionCommand().equals(event.getActionCommand())) {
				var disk1 = getActiveDiskPanel();
				var disk2 = getInactiveDiskPanel();
				if (disk1 != null && disk2 != null) {
					disk1.copyFile();
				}
			}
		});
		renamePRGButton = createButton(Resources.DROID64_BUTTON_RENAME, 'r', Button.RENAME_FILE_BUTTON, event -> {
			if (renamePRGButton.getActionCommand().equals(event.getActionCommand())) {
				var diskPanel = getActiveDiskPanel();
				if (diskPanel != null) {
					diskPanel.renameFile();
				}
			}
		});
		delPRGButton = createButton(Resources.DROID64_BUTTON_DELETE, 'd', Button.DELETE_FILE_BUTTON, event -> {
			if (delPRGButton.getActionCommand().equals(event.getActionCommand())) {
				var diskPanel = getActiveDiskPanel();
				if (diskPanel != null) {
					diskPanel.deleteFile();
				}
			}
		});
		newFileButton = createButton(Resources.DROID64_BUTTON_NEWFILE, 'w', Button.NEW_FILE_BUTTON, event -> {
			if (newFileButton.getActionCommand().equals(event.getActionCommand())) {
				var diskPanel = getActiveDiskPanel();
				if (diskPanel != null && diskPanel.isImageLoaded()) {
					diskPanel.newFile();
				} else {
					showErrorMessage(parent, LBL_NODISK);
				}
			}
		});
		mkdirButton = createButton(Resources.DROID64_BUTTON_MKDIR, 'k', Button.MKDIR_BUTTON, event -> {
			if (mkdirButton.getActionCommand().equals(event.getActionCommand())) {
				var diskPanel = getActiveDiskPanel();
				if (diskPanel != null ) {
					diskPanel.mkdir();
				}
			}
		});
		md5Button = createButton(Resources.DROID64_BUTTON_MD5, '5', Button.MD5_BUTTON, event -> {
			if (md5Button.getActionCommand().equals(event.getActionCommand()) && getActiveDiskPanel() != null ) {
				getActiveDiskPanel().calcMd5Checksum();
			}
		});
		searchButton = createButton(Resources.DROID64_BUTTON_SEARCH, 's', Button.SEARCH_BUTTON, event -> {
			if (searchButton.getActionCommand().equals(event.getActionCommand())) {
				new SearchPanel(DroiD64.PROGNAME + " - Search", this).showDialog();
			}
		});
	}

	private JPanel createConsolePanel() {
		var consolePanel = new JPanel(new BorderLayout());
		var border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		consolePanel.setBorder(BorderFactory.createTitledBorder(border, Utility.getMessage(Resources.DROID64_CONSOLE)));

		var popMenu = new JPopupMenu();
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_PROGRAM_CLEARCONSOLE, 'c', event -> consoleText.setText(Utility.EMPTY));
		GuiHelper.addMenuItem(popMenu, Resources.DROID64_MENU_PROGRAM_SAVECONSOLE, 'a', event -> saveConsole());

		consoleText.setEditable(false);
		consoleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, consoleText.getFont().getSize()));
		consoleText.setComponentPopupMenu(popMenu);
		consolePanel.add(new JScrollPane(consoleText), BorderLayout.CENTER);
		return consolePanel;
	}

	public void appendConsole(String message) {
		if (!Utility.EMPTY.equals(message)) {
			try {
				console.write(message);
				console.write('\n');
				console.flush();
			} catch (IOException ignore) { /* ignored */ }
		}
	}

	public void formatConsole(String message, Object...args) {
		appendConsole(String.format(message, args));
	}

	private void showErrorMessage(JFrame parent, String error){
		if (LBL_NODISK.equals(error)) {
			appendConsole("\nNo disk image file selected. Aborting.");
			JOptionPane.showMessageDialog(parent,
					Utility.getMessage(Resources.DROID64_INFO_NOIMAGELOADED),
					DroiD64.PROGNAME + " - No disk",
					JOptionPane.ERROR_MESSAGE);
		} else if (LBL_INSERTERROR.equals(error)) {
			appendConsole("\nInserting error. Aborting.\n");
			JOptionPane.showMessageDialog(parent,
					"An error occurred while inserting file into disk.\n"+
							"Look up console report message for further information.",
							DroiD64.PROGNAME + " - Failure while inserting file",
							JOptionPane.ERROR_MESSAGE );
		}
	}

	/**
	 * Create a help drag-down menu
	 * @return JMenu
	 */
	private JMenu createHelpMenu(JFrame parent) {
		var menu = new JMenu(Utility.getMessage(Resources.DROID64_MENU_HELP));
		menu.setMnemonic('h');
		ActionListener listener = event -> {
			var cmd = event.getActionCommand();
			if (Resources.DROID64_MENU_HELP_ABOUT.equals(cmd)) {
				showHelp();
			} else if (Resources.DROID64_MENU_HELP_TODO.equals(cmd)) {
				new BugsPanel().showDialog(parent);
			} else if (Resources.DROID64_MENU_HELP_RELEASENOTES.equals(cmd)) {
				new TextViewPanel(this).show(getReleaseNotes(), DroiD64.PROGNAME, Utility.getMessage(Resources.DROID64_MENU_HELP_RELEASENOTES), Utility.MIMETYPE_HTML);
			} else if (Resources.DROID64_MENU_HELP_MANUAL.equals(cmd)) {
				new TextViewPanel(this).show(getManual(), DroiD64.PROGNAME, Utility.getMessage(Resources.DROID64_MENU_HELP_MANUAL), Utility.MIMETYPE_HTML);
			} else if (Resources.DROID64_MENU_HELP_CONTACT.equals(cmd)) {
				var info = new JTextArea(Utility.getMessage(Resources.DROID64_MENU_HELP_CONTACT_MSG));
				info.setEditable(false);
				info.setLineWrap(true);
				var iPanel = new JPanel(new BorderLayout());
				iPanel.add(new JScrollPane(info), BorderLayout.CENTER);
				iPanel.addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(iPanel)));
				GuiHelper.setSize(info, 3, 4);
				JOptionPane.showMessageDialog(parent, iPanel, Utility.getMessage(Resources.DROID64_MENU_HELP_CONTACT), JOptionPane.INFORMATION_MESSAGE);
			}
		};
		menu.add(GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_HELP_ABOUT, 'a', listener));
		menu.add(GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_HELP_TODO, 'b', listener));
		menu.add(GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_HELP_RELEASENOTES, 'r', listener));
		menu.add(GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_HELP_MANUAL, 'm', listener));
		menu.add(GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_HELP_CONTACT, 'c', listener));
		return menu;
	}

	/**
	 * Create a help drag-down menu (just for testing)
	 * @param parent
	 * @return JMenu
	 */
	private JMenu createProgramMenu(final JFrame parent) {
		var menu = new JMenu(Utility.getMessage(Resources.DROID64_MENU_PROGRAM));
		menu.setMnemonic('P');
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_PROGRAM_SETTINGS, 's', event -> showSettings(parent));
		menu.addSeparator();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_PROGRAM_CLEARCONSOLE, 'c', event -> consoleText.setText(Utility.EMPTY));
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_PROGRAM_SAVECONSOLE, 'a', event -> saveConsole());
		menu.addSeparator();
		GuiHelper.addMenuItem(menu, Resources.DROID64_MENU_PROGRAM_EXIT, 'x', event -> exitThisProgram());
		return menu;
	}

	private void saveConsole() {
		var active = getActiveDiskPanel();
		var fileName = FileDialogHelper.openTextFileDialog(active != null ? active.getCurrentImagePath() : null, "console.txt", true);
		if (fileName != null) {
			try {
				Utility.writeFile(new File(fileName), consoleText.getText());
			} catch (CbmException e) {	//NOSONAR
				appendConsole("Failed to save console.\n"+e.getMessage());
			}
		}
	}

	private void setupBookmarkBar(BookmarkTree bt) {
		bookmarkBar.removeAll();
		bt.getButtons().forEach(bookmarkBar::add);
	}

	private JMenu createBookmarkMenu() {
		if (bookmarkMenu == null) {
			bookmarkMenu = new JMenu("Bookmarks");
			bookmarkMenu.setMnemonic('B');
		} else {
			bookmarkMenu.removeAll();
		}
		var manageBookmarks = new JMenuItem("Manage bookmarks...", 'M');
		var addBookmark = new JMenuItem("Add bookmark...", 'A');

		bookmarkMenu.add(manageBookmarks);
		bookmarkMenu.add(addBookmark);
		bookmarkMenu.addSeparator();

		var bookmarkFile = Setting.getBookmarkFile();
		appendConsole("Loading bookmarks from " + bookmarkFile.getAbsolutePath());

		var bt = new BookmarkTree(this, consoleStream);
		bt.load(bookmarkFile);
		bt.getMenuItems().forEach(bookmarkMenu::add);

		manageBookmarks.addActionListener(e -> {bt.showTree(); createBookmarkMenu();});

		addBookmark.addActionListener(e -> {
			var dp = getActiveDiskPanel();
			var p = dp.getCurrentPath();
			if (!Utility.isEmpty(p)) {
				var f = new File(p);
				var name = GuiHelper.getStringDialog(getParent(), "Add bookmark", "Name of bookmark", f.getName());
				if (!Utility.isEmpty(name)) {
					var b = new Bookmark();
					b.setPath(f.getAbsolutePath());
					b.setName(name);
					b.setCreated(new Date());
					b.setBookmarkType(Setting.isImageFileName(f) ? BookmarkType.DISKIMAGE : BookmarkType.DIRECTORY);
					b.setSelectedNo(dp.getSelectedRow());
					b.setPluginNo(-1);
					b.setDiskImageType(dp.getDiskImageType());
					b.setZipped(dp.isZipFileLoaded());
					bt.addEntry(b, null);
					createBookmarkMenu();
				}
			}
		});

		setupBookmarkBar(bt);
		return bookmarkMenu;
	}

	/**
	 * Setup search menu. Requires database.
	 * @param parent
	 * @return JMenu
	 */
	private JMenu createSearchMenu(final JFrame parent) {
		var mainPanel = this;
		searchMenu.setMnemonic('S');

		var searchMenuItem = new JMenuItem("Search...", 's');
		searchMenu.add (searchMenuItem);
		var scanMenuItem = new JMenuItem("Scan for disk images...", 'i');
		searchMenu.add (scanMenuItem);
		var syncMenuItem = new JMenuItem("Sync database and files", 'y');
		searchMenu.add (syncMenuItem);
		var exportMenuItem = new JMenuItem("Export database to XML..", 'e');
		searchMenu.add (exportMenuItem);

		searchMenuItem.addActionListener(ae -> new SearchPanel(DroiD64.PROGNAME+" - Search", mainPanel).showDialog());
		scanMenuItem.addActionListener(ae -> showScanForImages(parent));
		syncMenuItem.addActionListener(ae -> syncDatabase());
		exportMenuItem.addActionListener(ae -> exportDatabase());

		searchMenu.setEnabled(Setting.USE_DB.getBoolean());
		searchMenu.setToolTipText(Boolean.TRUE.equals(Setting.USE_DB.getBoolean()) ? null : "You must configure and enable database to use search.");
		searchMenu.setVisible(Setting.USE_DB.getBoolean());
		return searchMenu;
	}

	/**
	 * Good bye?
	 */
	private void exitThisProgram() {
		if (! Boolean.TRUE.equals(Setting.ASK_QUIT.getBoolean())|| JOptionPane.showConfirmDialog(
				parent, "Really quit?", "Leaving this program...",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			parent.dispose();
			System.exit(0);	//NOSONAR
		}
	}

	/**
	 * Show, edit and save settings.
	 */
	private void showSettings(JFrame parent) {
		doSettings(parent);
		new SettingsPanel(DroiD64.PROGNAME+" - Settings", this).showDialog();
		doSettings(parent);
	}

	/**
	 * Recursively search a folder for D64 images.
	 */
	private synchronized void showScanForImages(JFrame parent) {
		if (scannerActive) {
			GuiHelper.showErrorMessage(parent, "Scan failed", "Disk scanner is already active.");
		} else {
			var chooser = new JFileChooser(Setting.DEFAULT_IMAGE_DIR.getFile());
			chooser.setToolTipText("Select directory to start scanning for disk images in.");
			chooser.setDialogTitle("Choose directory to recursively scan for images");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				var excludePattern = Utility.isEmpty(Setting.EXCLUDED_IMAGE_FILES.getString()) ? null : Pattern.compile(Setting.EXCLUDED_IMAGE_FILES.getString());
				var dir = chooser.getSelectedFile();
				appendConsole("Scan for disk images in "+dir);
				var scanner = new Thread() {
					@Override
					public void run() {
						try {
							int numDisks = scanForD64Files(dir, excludePattern);
							GuiHelper.showInfoMessage(parent, "Scan completed", "Done scanning.%nFound %d disk images in %s.",numDisks, dir );
						} finally {
							scannerActive = false;
						}
					}
				};
				scannerActive = true;
				scanner.start();
			}
		}
	}

	/**
	 * Get all stored disks from database, and verify that files still exists in file system.
	 */
	private void syncDatabase() {
		try {
			var myHostName = Utility.getHostName();
			var map = DaoFactory.getDaoFactory().getDiskDao().getAllDisks(false)
				.filter(d -> d.getHostName() == null || d.getHostName().equals(myHostName))
				.map(disk -> {
					// map from disk to boolean which is true if it was deleted
					var f = new File(disk.getFilePath() + File.separator + disk.getFileName());
					if (!f.exists() || !f.isFile()) {
						try {
							appendConsole("Removing info for " + f.getPath());
							disk.setDelete();
							DaoFactory.getDaoFactory().getDiskDao().delete(disk);
							return Boolean.TRUE;
						} catch (DatabaseException ignored) { /* ignored */ }
					}
					return Boolean.FALSE;
				})
				.collect(Collectors.groupingBy(b -> b, Collectors.counting()));
			GuiHelper.showInfoMessage(parent, "Sync completed", "Sync done.%nRemoved %d of %d disk(s) from database.",
					Optional.ofNullable(map.get(true)).orElse(0L), Optional.ofNullable(map.get(false)).orElse(0L));
		} catch (DatabaseException e) {	//NOSONAR
			GuiHelper.showException(parent, "Sync failed", e, "Sync failed.");
		}
	}

	private void exportDatabase() {
		try {
			var chooser = new JFileChooser(Setting.DEFAULT_IMAGE_DIR.getFile());
			chooser.setToolTipText("Select XML file.");
			chooser.setDialogTitle("Export database to XML");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));
			if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				var f = chooser.getSelectedFile();
				appendConsole("Exporting database to " + f);
				var count = DiskList.export(f);
				appendConsole("Exported database with " + count + " disk images.");
			}
		} catch (DatabaseException | OutOfMemoryError e) {	//NOSONAR
			GuiHelper.showException(parent, "Export failed", e, "Export failed.");
		}
	}

	/**
	 * Apply settings to GUI
	 */
	private void doSettings(JFrame parent ) {
		setLookAndFeel(parent, Setting.LOOK_AND_FEEL.getInteger());
		searchMenu.setEnabled(Setting.USE_DB.getBoolean());
		searchMenu.setVisible(Setting.USE_DB.getBoolean());
		setDefaultFonts();
		consoleText.setFont(Setting.CONSOLE_FONT.getFont());

		if (Boolean.TRUE.equals(Setting.USE_DB.getBoolean())) {
			try {
				DaoFactoryImpl.initialize(Setting.JDBC_DRIVER.getString(), Setting.JDBC_URL.getString(), Setting.JDBC_USER.getString(), Setting.JDBC_PASS.getString(), Setting.MAX_ROWS.getInteger(), Setting.JDBC_LIMIT_TYPE.getInteger());
			} catch (DatabaseException e) {	//NOSONAR
				appendConsole("Load settings failed: "+e.getMessage());
			}
		}
	}

	private void setDefaultFonts() {
		GuiHelper.setDefaultFonts();
		if (diskPanel1 != null) {
			diskPanel1.setTableColors();
		}
		if (diskPanel2 != null) {
			diskPanel2.setTableColors();
		}
	}

	private void showHelp() {
		new ShowHelpPanel().showDialog(parent);
	}

	public static String[] getLookAndFeelNames() {
		var looks = new String[LOOK_AND_FEEL_CLASSES.length];
		for (var i=0; i < LOOK_AND_FEEL_CLASSES.length; i++) {
			var x = LOOK_AND_FEEL_CLASSES[i].split("[.]");
			looks[i] = x[x.length - 1];
		}
		return looks;
	}

	private void setLookAndFeel(JFrame parent, int lookAndFeel){
		try {
			var plaf = LOOK_AND_FEEL_CLASSES[lookAndFeel < LOOK_AND_FEEL_CLASSES.length && lookAndFeel >= 0 ? lookAndFeel: 1];
			UIManager.setLookAndFeel(plaf);
		} catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
			appendConsole("Look and feel failed: "+e);
		}
		try {
			var iconURL = getClass().getResource("/favicon.png");
			if (iconURL != null) {
				var icon = new ImageIcon(iconURL);
				parent.setIconImage(icon.getImage());
			}
		} catch (Exception e) {
			appendConsole("Icon image failed: "+e);
		}
		for (var entry : colorHashMap.entrySet()) {
			if (!(entry.getValue() instanceof javax.swing.plaf.ColorUIResource)) {
				continue;
			}
			var key = entry.getKey();
			var cr = (ColorUIResource) entry.getValue();
			switch (Setting.COLOUR.getInteger()) {
			case 0:	// gray (normal, no change to default values)
				UIManager.put(key, cr);
				break;
			case 1:		// red
				putColor(key, cr.getRed()+COLOUR_POWER_1, cr.getGreen()-COLOUR_POWER_1, cr.getBlue()-COLOUR_POWER_1);
				break;
			case 2:		// green
				putColor(key, cr.getRed()-COLOUR_POWER_1, cr.getGreen()+COLOUR_POWER_1, cr.getBlue()-COLOUR_POWER_1);
				break;
			case 3:		// blue
				putColor(key, cr.getRed()-COLOUR_POWER_1, cr.getGreen()-COLOUR_POWER_1, cr.getBlue()+COLOUR_POWER_1);
				break;
			case 4:		// gray-light
				putColor(key, cr.getRed()+COLOUR_POWER_2, cr.getGreen()+COLOUR_POWER_2, cr.getBlue()+COLOUR_POWER_2 + 10);
				break;
			case 5:		// gray-light
				putColor(key, cr.getRed()-COLOUR_POWER_2, cr.getGreen()-COLOUR_POWER_2, cr.getBlue()-COLOUR_POWER_2 + 10);
				break;
			case 6:		// cyan
				putColor(key, cr.getRed()-COLOUR_POWER_1, cr.getGreen()+COLOUR_POWER_2, cr.getBlue()+COLOUR_POWER_2 + 10);
				break;
			default:	// Unknown
				break;
			}
		}
		setDefaultFonts();
		SwingUtilities.updateComponentTreeUI(parent);
		parent.invalidate();
		parent.repaint();
	}

	private void putColor(Object key, int red, int green, int blue) {
		UIManager.put(key, new ColorUIResource(Utility.trimIntByte(red), Utility.trimIntByte(green), Utility.trimIntByte(blue)));
	}

	private void saveDefaultValues(){
		colorHashMap.clear();
		UIManager.getDefaults().entrySet().stream()
			.filter(e -> e.getValue() instanceof javax.swing.plaf.ColorUIResource)
			.forEach(entry -> colorHashMap.put(entry.getKey(), entry.getValue()));
	}

	public DiskPanel getLeftDiskPanel() {
		return this.diskPanel1;
	}
	public DiskPanel getRightDiskPanel() {
		return this.diskPanel2;
	}

	public JFrame getParent() {
		return parent;
	}

	private void setMainWindowSize(JFrame frame) {
		frame.pack();
		frame.setMinimumSize(new Dimension(64, 64));
		var splits = Setting.getWindow();
		if (splits.length < 4) {
			GuiHelper.setLocation(frame, 4, 4);
		} else {
			frame.setSize(splits[0], splits[1]);
			frame.setLocation(splits[2], splits[3]);
		}
	}

	/**
	 * Recursively scan dir for D64 images and add to database.
	 * @param dir directory to start searching in.
	 * @param excludePattern the regexp
	 * @return number of found disk images
	 */
	protected int scanForD64Files(File dir, Pattern excludePattern) {
		if (dir == null || dir.getName().startsWith(".")) {
			return 0;
		}

		appendConsole("Scanning " + dir);
		int diskCount = 0;
		try (var stream = Files.newDirectoryStream(dir.toPath(), p -> !p.getFileName().startsWith("."))) {
			Stream.Builder<Path> builder = Stream.builder();
			stream.forEach(builder::add);
			var files = builder.build().sorted().map(Path::toFile).collect(Collectors.toList());
			for (var file : files) {
				if (file.isDirectory()) {
					diskCount += scanForD64Files(file, excludePattern);
				} else if (file.isFile() && Setting.getDiskImageType(file) != DiskImageType.UNDEFINED
						&& (excludePattern == null || !excludePattern.matcher(file.getAbsolutePath()).matches())) {
					saveDiskToDatabase(file, dir);
					diskCount++;
				}
			}
			return diskCount;
		} catch (IOException e) {
			appendConsole("Error: " + e.getMessage());
			return diskCount;
		}
	}

	private void saveDiskToDatabase(File file, File dir) {
		try {
			var diskImage =  DiskImage.getDiskImage(file, consoleStream);
			diskImage.readBAM();
			diskImage.readDirectory();
			var disk = diskImage.getDisk();
			disk.setFilePath(dir.getAbsolutePath());
			disk.setFileName(file.getName());
			disk.setHostName(Utility.getHostName());
			DaoFactory.getDaoFactory().getDiskDao().save(disk);
			appendConsole("Saved info for " + file);
		} catch (DatabaseException | CbmException e) {	//NOSONAR
			appendConsole(file +" : "+e.getMessage());
		}
	}

	public void setPluginButtonLabel(int num, String label) {
		if (num < pluginButtons.length && pluginButtons[num] != null) {
			pluginButtons[num].setText(label);
		}
	}

	protected static String getReleaseNotes() {
		if (releaseNotes == null) {
			releaseNotes = getResource("resources/releasenotes.html");
		}
		return releaseNotes;
	}

	protected static String getManual() {
		if (manual == null) {
			manual = getResource("resources/manual.html");
		}
		return manual;
	}

	private static String getResource(String resourceFile) {
		try (var in = Setting.class.getResourceAsStream(resourceFile); var scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\Z").next();
		} catch (Exception e) {	//NOSONAR
			return "Failed to read " + resourceFile + " resource: \n"+e.getMessage();
		}
	}

	public void setConsoleFont(Font font) {
		consoleText.setFont(font);
	}

}
