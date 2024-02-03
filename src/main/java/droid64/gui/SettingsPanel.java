package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import droid64.d64.Utility;
import droid64.db.DaoFactory;
import droid64.db.DaoFactoryImpl;

/**
 * <pre style='font-family:Sans,Arial,Helvetica'>
 * Created on 30.06.2004
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
 * </pre>
 *
 * @author wolf
 */
public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String LBL_FOREGROUND = Utility.getMessage(Resources.DROID64_SETTINGS_FOREGROUND);
	private static final String LBL_BACKGROUND = Utility.getMessage(Resources.DROID64_SETTINGS_BACKGROUND);
	private static final String BROWSELABEL = "...";

	// GUI
	private final JCheckBox exitConfirmCheckBox = new JCheckBox(
			Utility.getMessage(Resources.DROID64_SETTINGS_CONFIRMEXIT));
	private final JCheckBox hideConsoleCheckBox = new JCheckBox(
			Utility.getMessage(Resources.DROID64_SETTINGS_HIDECONSOLE));
	private final JCheckBox bookmarkBarCheckBox = new JCheckBox(
			Utility.getMessage(Resources.DROID64_SETTINGS_BOOKMARKBAR));
	private final JComboBox<UIManager.LookAndFeelInfo> lookAndFeelBox = new JComboBox<>(UIManager.getInstalledLookAndFeels());
	private final JSpinner rowHeightSpinner = new JSpinner(
			new SpinnerNumberModel((int) Setting.ROW_HEIGHT.getInteger(), 8, 256, 1));
	private final JSpinner localRowHeightSpinner = new JSpinner(
			new SpinnerNumberModel((int) Setting.LOCAL_ROW_HEIGHT.getInteger(), 8, 256, 1));
	private final JTextField winSizePosField = new JTextField(getWindowSizePosString());

	// Files
	private final JTextField extRemoval = new JTextField(
			Setting.EXT_REMOVAL.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD64 = new JTextField(
			Setting.FILE_EXT_D64.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD67 = new JTextField(
			Setting.FILE_EXT_D67.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD71 = new JTextField(
			Setting.FILE_EXT_D71.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD80 = new JTextField(
			Setting.FILE_EXT_D80.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD81 = new JTextField(
			Setting.FILE_EXT_D81.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD82 = new JTextField(
			Setting.FILE_EXT_D82.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD88 = new JTextField(
			Setting.FILE_EXT_D88.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtLNX = new JTextField(
			Setting.FILE_EXT_LNX.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtT64 = new JTextField(
			Setting.FILE_EXT_T64.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD64gz = new JTextField(
			Setting.FILE_EXT_D64_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD67gz = new JTextField(
			Setting.FILE_EXT_D67_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD71gz = new JTextField(
			Setting.FILE_EXT_D71_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD80gz = new JTextField(
			Setting.FILE_EXT_D80_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD81gz = new JTextField(
			Setting.FILE_EXT_D81_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD82gz = new JTextField(
			Setting.FILE_EXT_D82_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtD88gz = new JTextField(
			Setting.FILE_EXT_D88_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtLNXgz = new JTextField(
			Setting.FILE_EXT_LNX_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	private final JTextField fileExtT64gz = new JTextField(
			Setting.FILE_EXT_T64_GZ.getList().stream().collect(Collectors.joining(Setting.DELIM)));
	// Colors
	private final JComboBox<String> colourBox = new JComboBox<>(COLORS);
	private final JButton colorFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorBgButton = new JButton(LBL_BACKGROUND);
	private final JButton colorCpmFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorCpmBgButton = new JButton(LBL_BACKGROUND);
	private final JButton colorLocalFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorLocalBgButton = new JButton(LBL_BACKGROUND);

	private Font cbmFont = Setting.CBM_FONT.getFont();
	private Font sysFont = Setting.SYS_FONT.getFont();
	private Font consoleFont = Setting.CONSOLE_FONT.getFont();

	// Plugin settings
	private final JTextField[] pluginLabelTextField = new JTextField[Setting.MAX_PLUGINS];
	private final FilePathPanel[] pluginCommandField = new FilePathPanel[Setting.MAX_PLUGINS];
	private final JTextArea[] pluginArgumentTextField = new JTextArea[Setting.MAX_PLUGINS];
	private final JTextArea[] pluginDescriptionTextField = new JTextArea[Setting.MAX_PLUGINS];
	private final JCheckBox[] forkThreadCheckBox = new JCheckBox[Setting.MAX_PLUGINS];

	// Database settings
	private final JCheckBox useJdbcCheckBox = new JCheckBox(Utility.getMessage(Resources.DROID64_SETTINGS_JDBC_USEDB));
	private final JTextField jdbcDriver = new JTextField(Setting.JDBC_DRIVER.getString());
	private final JTextField jdbcUrl = new JTextField(Setting.JDBC_URL.getString());
	private final JTextField jdbcUser = new JTextField(Setting.JDBC_USER.getString());
	private final JPasswordField jdbcPassword = new JPasswordField(Setting.JDBC_PASS.getString());
	private final JFormattedTextField maxRows = SearchPanel.getNumericField(Setting.MAX_ROWS.getInteger(), 8);
	private final JComboBox<String> limitTypeBox = new JComboBox<>(DaoFactory.getLimitNames());
	private final List<String> jdbcDriverClasses = GuiHelper.getClassNames(java.sql.Driver.class);
	private final JTextField excludeImageFiles = new JTextField(Setting.EXCLUDED_IMAGE_FILES.getString());

	/** Colors */
	private static final String[] COLORS = { "gray", "red", "green", "blue", "light-blue", "dark-grey", "cyan" };

	private final JTextArea status = new JTextArea();

	private static final String ARGUMENT_TOOLTIP = Utility.getMessage(Resources.PLUGIN_ARGUMENTS_TOOLTIP);

	private static String sqlSetupScript = null;
	private final MainPanel mainPanel;
	private final String title;

	/**
	 * Constructor
	 *
	 * @param title     String
	 * @param mainPanel MainPanel
	 */
	public SettingsPanel(String title, MainPanel mainPanel) {
		this.title = title;
		this.mainPanel = mainPanel;
			
		lookAndFeelBox.setRenderer(new BasicComboBoxRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				var text = Optional.ofNullable(value)
						.filter(UIManager.LookAndFeelInfo.class::isInstance)
						.map(UIManager.LookAndFeelInfo.class::cast)
						.map(UIManager.LookAndFeelInfo::getName)
						.orElse(String.valueOf(value));
				setText(text);
				return this;
			}
		});

		setLayout(new BorderLayout());
		var tabPane = new JTabbedPane();
		tabPane.addTab(Utility.getMessage(Resources.DROID64_SETTINGS_TAB_GUI), new JScrollPane(drawGuiPanel()));
		tabPane.addTab(Utility.getMessage(Resources.DROID64_SETTINGS_TAB_FILES), new JScrollPane(drawFilesPanel()));
		tabPane.addTab(Utility.getMessage(Resources.DROID64_SETTINGS_TAB_COLORS), new JScrollPane(drawColorPanel()));
		tabPane.addTab(Utility.getMessage(Resources.DROID64_SETTINGS_TAB_DATABASE), new JScrollPane(drawDatabasePanel()));

		var pluginTabPane = new JTabbedPane();
		var pluginPanel = drawPluginPanel();
		for (var i = 0; i < Setting.MAX_PLUGINS; i++) {
			pluginTabPane.addTab(Integer.toString(i + 1), pluginPanel[i]);
		}
		tabPane.addTab(Utility.getMessage(Resources.DROID64_SETTINGS_TAB_PLUGIN), new JScrollPane(pluginTabPane));

		add(tabPane, BorderLayout.CENTER);

		add(drawGeneralPanel(), BorderLayout.SOUTH);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void showDialog() {
		JOptionPane.showMessageDialog(mainPanel.getParent(), this, title, JOptionPane.PLAIN_MESSAGE);
		copyValuesToSettings(mainPanel);
		try {
			Setting.save();
		} catch (IOException e) {
			mainPanel.appendConsole(e.getMessage());
		}
	}

	/**
	 * Setup up general settings panel
	 *
	 * @return JPanel
	 */
	private JPanel drawGeneralPanel() {
		var txtPanel = new JPanel();
		txtPanel.setAlignmentX(CENTER_ALIGNMENT);
		txtPanel.add(new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_SAVEMESSAGE)));
		var generalPanel = new JPanel(new BorderLayout());
		generalPanel.add(txtPanel, BorderLayout.NORTH);
		return generalPanel;
	}
	
	private JPanel drawFilesPanel() {
		var guiPanel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		var fp1 = new FilePathPanel(Setting.DEFAULT_IMAGE_DIR.getFile(), JFileChooser.DIRECTORIES_ONLY,
				Setting.DEFAULT_IMAGE_DIR::set);
		var fp2 = new FilePathPanel(Setting.DEFAULT_IMAGE_DIR2.getFile(), JFileChooser.DIRECTORIES_ONLY,
				Setting.DEFAULT_IMAGE_DIR2::set);

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, guiPanel,
				new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_LEFTDIR)));
		GuiHelper.addToGridBag(1, 0, 1.0, 0.0, 2, gbc, guiPanel, fp1);

		GuiHelper.addToGridBag(0, 1, 0.0, 0.0, 1, gbc, guiPanel,
				new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_RIGHTDIR)));
		GuiHelper.addToGridBag(1, 1, 1.0, 0.0, 2, gbc, guiPanel, fp2);

		GuiHelper.addToGridBag(0, 2, 0.0, 0.0, 1, gbc, guiPanel,
				new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXT_REMOVAL)));
		GuiHelper.addToGridBag(1, 2, 1.0, 0.0, 1, gbc, guiPanel, extRemoval);
		GuiHelper.addToGridBag(2, 2, 1.0, 0.0, 1, gbc, guiPanel, new JPanel());

		addFields(3, Resources.DROID64_SETTINGS_EXT_D64, fileExtD64, fileExtD64gz, guiPanel, gbc);
		addFields(4, Resources.DROID64_SETTINGS_EXT_D67, fileExtD67, fileExtD67gz, guiPanel, gbc);
		addFields(5, Resources.DROID64_SETTINGS_EXT_D71, fileExtD71, fileExtD71gz, guiPanel, gbc);
		addFields(6, Resources.DROID64_SETTINGS_EXT_D80, fileExtD80, fileExtD80gz, guiPanel, gbc);
		addFields(7, Resources.DROID64_SETTINGS_EXT_D81, fileExtD81, fileExtD81gz, guiPanel, gbc);
		addFields(8, Resources.DROID64_SETTINGS_EXT_D82, fileExtD82, fileExtD82gz, guiPanel, gbc);
		addFields(9, Resources.DROID64_SETTINGS_EXT_D88, fileExtD88, fileExtD88gz, guiPanel, gbc);
		addFields(10, Resources.DROID64_SETTINGS_EXT_T64, fileExtT64, fileExtT64gz, guiPanel, gbc);
		addFields(11, Resources.DROID64_SETTINGS_EXT_LNX, fileExtLNX, fileExtLNXgz, guiPanel, gbc);

		GuiHelper.addToGridBag(0, 12, 1.0, 0.8, 3, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	private void addFields(int row, String propertyKey, JComponent field1, JComponent field2, JPanel panel,
			GridBagConstraints gbc) {
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, panel, new JLabel(Utility.getMessage(propertyKey)));
		GuiHelper.addToGridBag(1, row, 0.5, 0.0, 1, gbc, panel, field1);
		GuiHelper.addToGridBag(2, row, 0.5, 0.0, 1, gbc, panel, field2);
	}

	private String getWindowSizePosString() {
		var splits = Setting.getWindow();
		if (splits.length < 4) {
			return "";
		}
		return String.format("%d:%d,%d:%d", splits[0], splits[1], splits[2], splits[3]);
	}

	/**
	 * Setup panel with GUI settings
	 *
	 * @param mainPanel
	 * @return JPanel
	 */
	private JPanel drawGuiPanel() {
		var guiPanel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		exitConfirmCheckBox.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_CONFIRMEXIT_TOOLTIP));
		exitConfirmCheckBox.setSelected(Setting.ASK_QUIT.getBoolean());

		hideConsoleCheckBox.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_HIDECONSOLE_TOOLTIP));
		hideConsoleCheckBox.setSelected(Setting.HIDECONSOLE.getBoolean());

		bookmarkBarCheckBox.setSelected(Setting.BOOKMARK_BAR.getBoolean());

		lookAndFeelBox.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_LOOKFEEL_TOOLTIP));
		lookAndFeelBox.setEditable(false);
		lookAndFeelBox.setSelectedIndex(0);
		var laf=GuiHelper.getLookAndFeels().stream().filter(f->f.getClassName().equals(Setting.LOOK_AND_FEEL.getString())).findFirst().orElse(null);
		lookAndFeelBox.setSelectedItem(laf);

		rowHeightSpinner.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_GRIDSPACING_TOOLTIP));

		localRowHeightSpinner.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_GRIDSPACING_TOOLTIP));

		var fontSizeSpinner = new JSpinner(
				new SpinnerNumberModel(Setting.FONT_SIZE.getInteger().intValue(), 8, 144, 1));
		var localFontSizeSpinner = new JSpinner(
				new SpinnerNumberModel(Setting.LOCAL_FONT_SIZE.getInteger().intValue(), 8, 144, 1));
		fontSizeSpinner.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_FONTSIZE_TOOLTIP));
		localFontSizeSpinner.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_FONTSIZE_TOOLTIP));

		fontSizeSpinner.addChangeListener(
				event -> Setting.FONT_SIZE.set(Integer.parseInt(fontSizeSpinner.getValue().toString())));
		localFontSizeSpinner.addChangeListener(
				event -> Setting.LOCAL_FONT_SIZE.set(Integer.parseInt(localFontSizeSpinner.getValue().toString())));

		var getWinSizeButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_WINDOWSIZE_FETCH));
		getWinSizeButton.addActionListener(ae -> {
			Dimension size = mainPanel.getParent().getSize();
			Point location = mainPanel.getParent().getLocation();
			String str = String.format("%d:%d,%d:%d", (int) size.getWidth(), (int) size.getHeight(),
					(int) location.getX(), (int) location.getY());
			winSizePosField.setText(str);
		});
		// system font
		var sysFontField = new JTextField(OldParameter.getFontAsDisplayString(sysFont));
		var sysBundledFont = new JCheckBox(Utility.EMPTY, sysFont == null);
		sysFontField.setEditable(false);
		var browseSysFontButton = new JButton(BROWSELABEL);
		browseSysFontButton.addActionListener(event -> {
			Font font = new FontChooser(mainPanel.getParent(), "Select system font").show(sysFont);
			if (font != null) {
				sysFont = font;
				sysFontField.setText(OldParameter.getFontAsDisplayString(font));
				sysBundledFont.setEnabled(false);
			}
		});
		sysBundledFont.addItemListener(event -> {
			if (sysBundledFont.isSelected()) {
				sysFont = null;
				sysFontField.setText(Utility.EMPTY);
			} else if (sysFont == null) {
				sysBundledFont.setSelected(true);
			}
		});

		// system font
		var consoleFontField = new JTextField(OldParameter.getFontAsDisplayString(consoleFont));
		var consoleBundledFont = new JCheckBox(Utility.EMPTY, consoleFont == null);
		consoleFontField.setEditable(false);
		var browseConsoleFontButton = new JButton(BROWSELABEL);
		browseConsoleFontButton.addActionListener(event -> {
			Font font = new FontChooser(mainPanel.getParent(), "Select console font").show(consoleFont);
			if (font != null) {
				consoleFont = font;
				consoleFontField.setText(OldParameter.getFontAsDisplayString(font));
				consoleBundledFont.setEnabled(false);
			}
		});
		consoleBundledFont.addItemListener(event -> {
			if (consoleBundledFont.isSelected()) {
				consoleFont = null;
				consoleFontField.setText(Utility.EMPTY);
			} else if (consoleFont == null) {
				consoleBundledFont.setSelected(true);
			}
		});

		// commodore font
		var cbmFontField = new JTextField(OldParameter.getFontAsDisplayString(cbmFont));
		var cbmBundledFont = new JCheckBox(Utility.EMPTY, cbmFont == null);
		cbmFontField.setEditable(false);
		var browseCbmFontButton = new JButton(BROWSELABEL);
		browseCbmFontButton.addActionListener(event -> {
			var font = new FontChooser(mainPanel.getParent(), "Select Commodore font").show(cbmFont);
			if (font != null) {
				cbmFont = font;
				cbmFontField.setText(OldParameter.getFontAsDisplayString(font));
				cbmBundledFont.setSelected(false);
			}
		});
		cbmBundledFont.addItemListener(event -> {
			if (cbmBundledFont.isSelected()) {
				cbmFont = null;
				cbmFontField.setText(Utility.EMPTY);
			} else if (cbmFont == null) {
				cbmBundledFont.setSelected(true);
			}
		});
		// font panels
		var sysFontPanelButtons = new JPanel(new BorderLayout());
		sysFontPanelButtons.add(sysBundledFont, BorderLayout.WEST);
		sysFontPanelButtons.add(browseSysFontButton, BorderLayout.EAST);
		var sysFontFilePanel = new JPanel(new BorderLayout());
		sysFontFilePanel.add(sysFontField, BorderLayout.CENTER);
		sysFontFilePanel.add(sysFontPanelButtons, BorderLayout.EAST);

		var consoleFontPanelButtons = new JPanel(new BorderLayout());
		consoleFontPanelButtons.add(consoleBundledFont, BorderLayout.WEST);
		consoleFontPanelButtons.add(browseConsoleFontButton, BorderLayout.EAST);
		var consoleFontFilePanel = new JPanel(new BorderLayout());
		consoleFontFilePanel.add(consoleFontField, BorderLayout.CENTER);
		consoleFontFilePanel.add(consoleFontPanelButtons, BorderLayout.EAST);

		var cbmFontPanelButtons = new JPanel(new BorderLayout());
		cbmFontPanelButtons.add(cbmBundledFont, BorderLayout.WEST);
		cbmFontPanelButtons.add(browseCbmFontButton, BorderLayout.EAST);
		var cbmFontFilePanel = new JPanel(new BorderLayout());
		cbmFontFilePanel.add(cbmFontField, BorderLayout.CENTER);
		cbmFontFilePanel.add(cbmFontPanelButtons, BorderLayout.EAST);

		var winSizePanel = new JPanel(new BorderLayout());
		winSizePanel.add(winSizePosField, BorderLayout.CENTER);
		winSizePanel.add(getWinSizeButton, BorderLayout.EAST);

		var fontSizePanel1 = new JPanel(new BorderLayout());
		fontSizePanel1.add(fontSizeSpinner, BorderLayout.WEST);
		fontSizePanel1.add(new JPanel(), BorderLayout.CENTER);

		var fontSizePanel2 = new JPanel(new BorderLayout());
		fontSizePanel2.add(localFontSizeSpinner, BorderLayout.WEST);
		fontSizePanel2.add(new JPanel(), BorderLayout.CENTER);

		var checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkboxPanel.add(exitConfirmCheckBox);
		checkboxPanel.add(hideConsoleCheckBox);
		checkboxPanel.add(bookmarkBarCheckBox);

		var localRowHeightPanel = new JPanel(new BorderLayout());
		localRowHeightPanel.add(rowHeightSpinner, BorderLayout.WEST);
		localRowHeightPanel.add(new JPanel(), BorderLayout.CENTER);
		var imageRowHeightPanel = new JPanel(new BorderLayout());
		imageRowHeightPanel.add(localRowHeightSpinner, BorderLayout.WEST);
		imageRowHeightPanel.add(new JPanel(), BorderLayout.CENTER);

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, guiPanel, new JPanel());
		GuiHelper.addToGridBag(1, 0, 1.0, 0.0, 1, gbc, guiPanel, checkboxPanel);
		GuiHelper.addToGridBag(2, 0, 0.0, 0.0, 1, gbc, guiPanel, new JPanel());

		addField(1, Resources.DROID64_SETTINGS_LOOKFEEL, lookAndFeelBox, guiPanel, gbc);
		addField(2, Resources.DROID64_SETTINGS_GRIDSPACING_IMAGE, localRowHeightPanel, guiPanel, gbc);
		addField(3, Resources.DROID64_SETTINGS_GRIDSPACING_LOCAL, imageRowHeightPanel, guiPanel, gbc);
		addField(4, Resources.DROID64_SETTINGS_FONTSIZE_IMAGE, fontSizePanel1, guiPanel, gbc);
		addField(5, Resources.DROID64_SETTINGS_FONTSIZE_LOCAL, fontSizePanel2, guiPanel, gbc);
		addField(6, Resources.DROID64_SETTINGS_WINDOWSIZE, winSizePanel, guiPanel, gbc);
		addField(7, Resources.DROID64_SETTINGS_SYSFONT, sysFontFilePanel, guiPanel, gbc);
		addField(8, Resources.DROID64_SETTINGS_CBMFONT, cbmFontFilePanel, guiPanel, gbc);
		addField(9, Resources.DROID64_SETTINGS_CONSOLEFONT, consoleFontFilePanel, guiPanel, gbc);
		GuiHelper.addToGridBag(0, 10, 0.5, 0.8, 3, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	private void addField(int row, String propertyKey, JComponent component, JPanel panel, GridBagConstraints gbc) {
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, panel, new JLabel(Utility.getMessage(propertyKey)));
		GuiHelper.addToGridBag(1, row, 1.0, 0.0, 1, gbc, panel, component);
		GuiHelper.addToGridBag(2, row, 0.0, 0.0, 1, gbc, panel, new JPanel());
	}

	private void copyValuesToSettings(final MainPanel mainPanel) {
		Setting.ASK_QUIT.set(exitConfirmCheckBox.isSelected());
		Setting.BOOKMARK_BAR.set(bookmarkBarCheckBox.isSelected());
		Setting.HIDECONSOLE.set(hideConsoleCheckBox.isSelected());
		Setting.COLOUR.set(colourBox.getSelectedIndex());
		Setting.ROW_HEIGHT.set(Integer.parseInt(rowHeightSpinner.getValue().toString()));
		Setting.LOOK_AND_FEEL.set(Optional.ofNullable(lookAndFeelBox.getSelectedItem())
				.map(UIManager.LookAndFeelInfo.class::cast)
				.map(UIManager.LookAndFeelInfo::getClassName)
				.orElse(Setting.DEFAULT_LOOK_AND_FEEL_CLASS));
		Setting.LOCAL_ROW_HEIGHT.set(Integer.parseInt(localRowHeightSpinner.getValue().toString()));
		Setting.USE_DB.set(useJdbcCheckBox.isSelected());
		Setting.JDBC_DRIVER.set(jdbcDriver.getText());
		Setting.JDBC_URL.set(jdbcUrl.getText());
		Setting.JDBC_USER.set(jdbcUser.getText());
		Setting.JDBC_PASS.set(String.valueOf(jdbcPassword.getPassword()));
		Setting.JDBC_LIMIT_TYPE.set(limitTypeBox.getSelectedIndex());
		Setting.EXCLUDED_IMAGE_FILES.set(excludeImageFiles.getText());
		Setting.WINDOW.set(winSizePosField.getText());
		Setting.SYS_FONT.set(sysFont);
		Setting.CBM_FONT.set(cbmFont);
		Setting.CONSOLE_FONT.set(consoleFont);

		Setting.DIR_BG.set(colorBgButton.getBackground());
		Setting.DIR_FG.set(colorFgButton.getForeground());
		Setting.DIR_CPM_BG.set(colorCpmBgButton.getBackground());
		Setting.DIR_CPM_FG.set(colorCpmFgButton.getForeground());
		Setting.DIR_LOCAL_BG.set(colorLocalBgButton.getBackground());
		Setting.DIR_LOCAL_FG.set(colorLocalFgButton.getForeground());

		Setting.EXT_REMOVAL.set(extRemoval.getText());
		Setting.FILE_EXT_D64.set(fileExtD64.getText());
		Setting.FILE_EXT_D67.set(fileExtD67.getText());
		Setting.FILE_EXT_D71.set(fileExtD71.getText());
		Setting.FILE_EXT_D80.set(fileExtD80.getText());
		Setting.FILE_EXT_D81.set(fileExtD81.getText());
		Setting.FILE_EXT_D82.set(fileExtD82.getText());
		Setting.FILE_EXT_D88.set(fileExtD88.getText());
		Setting.FILE_EXT_LNX.set(fileExtLNX.getText());
		Setting.FILE_EXT_T64.set(fileExtT64.getText());

		Setting.FILE_EXT_D64_GZ.set(fileExtD64gz.getText());
		Setting.FILE_EXT_D67_GZ.set(fileExtD67gz.getText());
		Setting.FILE_EXT_D71_GZ.set(fileExtD71gz.getText());
		Setting.FILE_EXT_D80_GZ.set(fileExtD80gz.getText());
		Setting.FILE_EXT_D81_GZ.set(fileExtD81gz.getText());
		Setting.FILE_EXT_D82_GZ.set(fileExtD82gz.getText());
		Setting.FILE_EXT_D88_GZ.set(fileExtD88gz.getText());
		Setting.FILE_EXT_LNX_GZ.set(fileExtLNXgz.getText());
		Setting.FILE_EXT_T64_GZ.set(fileExtT64gz.getText());

		try {
			Setting.MAX_ROWS.set(Integer.parseInt(maxRows.getText()));
		} catch (NumberFormatException e) {
			maxRows.setValue(25L);
		}

		for (var i = 0; i < pluginCommandField.length; i++) {
			mainPanel.setPluginButtonLabel(i, pluginLabelTextField[i].getText());
			var prg = new ExternalProgram(pluginCommandField[i].getPath(), pluginArgumentTextField[i].getText(),
					pluginDescriptionTextField[i].getText(), pluginLabelTextField[i].getText(),
					forkThreadCheckBox[i].isSelected());
			Setting.setExternalProgram(i, prg);
		}
	}

	private void setupColorButton(final Setting fgSetting, final Setting bgSetting, final JButton fgButton,
			final JButton bgButton) {
		fgButton.setForeground(fgSetting.getColor());
		fgButton.setBackground(bgSetting.getColor());
		bgButton.setForeground(fgSetting.getColor());
		bgButton.setBackground(bgSetting.getColor());

		fgButton.addActionListener(event -> {
			var c = JColorChooser.showDialog(fgButton, LBL_FOREGROUND, fgButton.getForeground());
			fgButton.setForeground(c);
			bgButton.setForeground(c);
		});
		bgButton.addActionListener(event -> {
			var c = JColorChooser.showDialog(bgButton, LBL_BACKGROUND, bgButton.getBackground());
			fgButton.setBackground(c);
			bgButton.setBackground(c);
		});
	}

	/**
	 * Setup Color settings
	 *
	 * @return JPanel
	 */
	private JPanel drawColorPanel() {

		colourBox.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_COLOR_SCHEME_TOOLTIP));
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(Setting.COLOUR.getInteger() < COLORS.length ? Setting.COLOUR.getInteger() : 0);

		setupColorButton(Setting.DIR_FG, Setting.DIR_BG, colorFgButton, colorBgButton);
		setupColorButton(Setting.DIR_CPM_FG, Setting.DIR_CPM_BG, colorCpmFgButton, colorCpmBgButton);
		setupColorButton(Setting.DIR_LOCAL_FG, Setting.DIR_LOCAL_BG, colorLocalFgButton, colorLocalBgButton);

		var colorActiveBorderButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_COLOR_ACTIVE));
		colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(Setting.BORDER_ACTIVE.getColor(), 3));
		colorActiveBorderButton.addActionListener(ae -> {
			var c = JColorChooser.showDialog(colorActiveBorderButton, LBL_FOREGROUND, Setting.BORDER_ACTIVE.getColor());
			colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
			Setting.BORDER_ACTIVE.set(c);
		});
		var colorInactiveBorderButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_COLOR_INACTIVE));
		colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(Setting.BORDER_INACTIVE.getColor(), 3));
		colorInactiveBorderButton.addActionListener(ae -> {
			var c = JColorChooser.showDialog(colorInactiveBorderButton, LBL_BACKGROUND,
					Setting.BORDER_INACTIVE.getColor());
			colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
			Setting.BORDER_INACTIVE.set(c);
		});

		var colorResetButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_COLOR_RESET));
		colorResetButton.addActionListener(ae -> resetButtonColors(colorActiveBorderButton, colorInactiveBorderButton));

		var colorPanel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFields(0, Resources.DROID64_SETTINGS_COLOR_THEME, colourBox, new JPanel(), colorPanel, gbc);
		addFields(1, Utility.EMPTY, colorResetButton, new JPanel(), colorPanel, gbc);
		addFields(2, Resources.DROID64_SETTINGS_COLOR_CBM, colorFgButton, colorBgButton, colorPanel, gbc);
		addFields(3, Resources.DROID64_SETTINGS_COLOR_CPM, colorCpmFgButton, colorCpmBgButton, colorPanel, gbc);
		addFields(4, Resources.DROID64_SETTINGS_COLOR_LOCAL, colorLocalFgButton, colorLocalBgButton, colorPanel, gbc);
		addFields(5, Resources.DROID64_SETTINGS_COLOR_BORDER, colorActiveBorderButton, colorInactiveBorderButton,
				colorPanel, gbc);

		GuiHelper.addToGridBag(0, 6, 0.5, 0.8, 3, gbc, colorPanel, new JPanel());
		return colorPanel;
	}

	private void resetButtonColors(JButton colorActiveBorder, JButton colorInactiveBorder) {

		Setting.DIR_BG.reset();
		Setting.DIR_FG.reset();
		Setting.DIR_LOCAL_BG.reset();
		Setting.DIR_LOCAL_FG.reset();
		Setting.DIR_CPM_FG.reset();
		Setting.DIR_CPM_FG.reset();
		Setting.BORDER_ACTIVE.reset();
		Setting.BORDER_INACTIVE.reset();

		colorFgButton.setForeground(Setting.DIR_FG.getColor());
		colorFgButton.setBackground(Setting.DIR_BG.getColor());
		colorBgButton.setForeground(Setting.DIR_FG.getColor());
		colorBgButton.setBackground(Setting.DIR_BG.getColor());
		colorLocalFgButton.setForeground(Setting.DIR_LOCAL_FG.getColor());
		colorLocalFgButton.setBackground(Setting.DIR_LOCAL_BG.getColor());
		colorLocalBgButton.setForeground(Setting.DIR_LOCAL_FG.getColor());
		colorLocalBgButton.setBackground(Setting.DIR_LOCAL_BG.getColor());
		colorCpmFgButton.setForeground(Setting.DIR_CPM_FG.getColor());
		colorCpmFgButton.setBackground(Setting.DIR_CPM_BG.getColor());
		colorCpmBgButton.setForeground(Setting.DIR_CPM_FG.getColor());
		colorCpmBgButton.setBackground(Setting.DIR_CPM_BG.getColor());

		colorActiveBorder.setBorder(BorderFactory.createLineBorder(Setting.BORDER_ACTIVE.getColor()));
		colorInactiveBorder.setBorder(BorderFactory.createLineBorder(Setting.BORDER_INACTIVE.getColor()));
	}

	/**
	 * Create the panel with database settings.
	 *
	 * @return JPanel
	 */
	private JPanel drawDatabasePanel() {
		var dbPanel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		useJdbcCheckBox.setSelected(Setting.USE_DB.getBoolean());
		var testConnectionButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_JDBC_TEST));
		status.setFont(new Font("Verdana", Font.PLAIN, status.getFont().getSize()));
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		status.setEditable(false);
		useJdbcCheckBox.addActionListener(ae -> {
			boolean enabled = useJdbcCheckBox.isSelected();
			jdbcDriver.setEnabled(enabled);
			jdbcUrl.setEnabled(enabled);
			jdbcUser.setEnabled(enabled);
			jdbcPassword.setEnabled(enabled);
			maxRows.setEnabled(enabled);
			testConnectionButton.setEnabled(enabled);
		});

		testConnectionButton.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_JDBC_TEST_TOOLTIP));
		testConnectionButton.addActionListener(ae -> {
			var errStr = DaoFactoryImpl.testConnection(jdbcDriver.getText(), jdbcUrl.getText(), jdbcUser.getText(),
					new String(jdbcPassword.getPassword()));
			status.setText(errStr == null ? Utility.getMessage(Resources.DROID64_SETTINGS_OK) : errStr);
		});

		boolean jdbcEnabled = useJdbcCheckBox.isSelected();
		jdbcDriver.setEnabled(jdbcEnabled);
		jdbcUrl.setEnabled(jdbcEnabled);
		jdbcUser.setEnabled(jdbcEnabled);
		jdbcPassword.setEnabled(jdbcEnabled);
		maxRows.setEnabled(jdbcEnabled);
		testConnectionButton.setEnabled(jdbcEnabled);

		limitTypeBox.setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_JDBC_LIMIT_TOOLTIP));
		limitTypeBox.setEditable(false);
		limitTypeBox.setSelectedIndex(0);
		limitTypeBox.setSelectedIndex(Setting.JDBC_LIMIT_TYPE.getInteger() < DaoFactory.getLimitNames().length
				? Setting.JDBC_LIMIT_TYPE.getInteger()
				: 0);

		excludeImageFiles.setToolTipText(Utility.getResource(Resources.DROID64_SETTINGS_EXCLUDE_IMAGE_FILES_TOOLTIP));

		var jdbcDriverBrowse = new JButton(BROWSELABEL);
		jdbcDriverBrowse.setEnabled(!jdbcDriverClasses.isEmpty());
		jdbcDriverBrowse.addActionListener(e -> browseJdbcDrivers());

		if (jdbcDriver.getText().isEmpty() && jdbcDriverClasses.size() == 1) {
			jdbcDriver.setText(jdbcDriverClasses.get(0));
		}

		var sqlString = Utility.getMessage("jdbc.feature.message", Utility.getMessage(Resources.JDBC_MYSQL_URL),
				Utility.getMessage(Resources.JDBC_POSTGRESQL_URL));
		var sql = getSqlSetupScript(mainPanel);
		var viewSqlButton = new JButton(Utility.getMessage(Resources.DROID64_SETTINGS_JDBC_SQL));
		viewSqlButton.addActionListener(
				ae -> new TextViewPanel(mainPanel).show(sql, "DroiD64", "SQL database setup", Utility.MIMETYPE_TEXT));

		var messageTextArea = new JTextArea(10, 45);
		messageTextArea.setBackground(new Color(230, 230, 230));
		messageTextArea.setEditable(false);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setText(sqlString);
		messageTextArea.setCaretPosition(0);

		var buttonPanel = new JPanel();
		buttonPanel.add(viewSqlButton);
		buttonPanel.add(testConnectionButton);
		buttonPanel.add(new JPanel());

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		GuiHelper.addToGridBag(1, 0, 0.5, 0.0, 1, gbc, dbPanel, useJdbcCheckBox);
		GuiHelper.addToGridBag(2, 0, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());

		var p = new JPanel(new BorderLayout());
		p.add(jdbcDriver, BorderLayout.CENTER);
		p.add(jdbcDriverBrowse, BorderLayout.EAST);

		addField(1, Resources.DROID64_SETTINGS_JDBC_CLASS, p, dbPanel, gbc);
		addField(2, Resources.DROID64_SETTINGS_JDBC_URL, jdbcUrl, dbPanel, gbc);
		addField(3, Resources.DROID64_SETTINGS_JDBC_USER, jdbcUser, dbPanel, gbc);
		addField(4, Resources.DROID64_SETTINGS_JDBC_PASS, jdbcPassword, dbPanel, gbc);
		addField(5, Resources.DROID64_SETTINGS_JDBC_ROWS, maxRows, dbPanel, gbc);
		addField(6, Resources.DROID64_SETTINGS_JDBC_LIMIT, limitTypeBox, dbPanel, gbc);

		addField(7, Resources.DROID64_SETTINGS_EXCLUDE_IMAGE_FILES, excludeImageFiles, dbPanel, gbc);

		GuiHelper.addToGridBag(0, 8, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		GuiHelper.addToGridBag(1, 8, 0.5, 0.0, 2, gbc, dbPanel, buttonPanel);

		addField(9, Resources.DROID64_SETTINGS_JDBC_STATUS, status, dbPanel, gbc);

		GuiHelper.addToGridBag(0, 10, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		gbc.fill = GridBagConstraints.BOTH;
		GuiHelper.addToGridBag(1, 10, 0.5, 0.9, 1, gbc, dbPanel, new JScrollPane(messageTextArea));

		return dbPanel;
	}

	private void browseJdbcDrivers() {
		var classModel = new DefaultListModel<String>();
		jdbcDriverClasses.forEach(classModel::addElement);
		var classList = new JList<String>(classModel);
		classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		classList.setSelectedValue(Setting.JDBC_DRIVER.getString(), true);
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, new JScrollPane(classList),
				"Choose JDBC driver", JOptionPane.OK_CANCEL_OPTION)) {
			var drv = classList.getSelectedValue();
			if (drv != null) {
				jdbcDriver.setText(drv);
			}
		}
	}

	/**
	 * Setup MAX_PLUGINS of panels for plugins.
	 *
	 * @param mainPanel
	 * @return JPanel[]
	 */
	private JPanel[] drawPluginPanel() {
		var pluginPanel = new JPanel[Setting.MAX_PLUGINS];

		var fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.canExecute();
			}

			@Override
			public String getDescription() {
				return "Executable";
			}
		};

		for (var i = 0; i < Setting.MAX_PLUGINS; i++) {
			pluginPanel[i] = new JPanel(new GridBagLayout());
			var gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;

			pluginLabelTextField[i] = getTextField(Resources.DROID64_SETTINGS_EXE_LABEL_TOOLTIP);

			pluginCommandField[i] = new FilePathPanel(Setting.DEFAULT_IMAGE_DIR.getFile(), JFileChooser.FILES_ONLY,
					null);
			pluginCommandField[i].setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_CHOOSE_TOOLTIP));
			pluginCommandField[i].setFileFilter(fileFilter);

			pluginArgumentTextField[i] = getTextArea(ARGUMENT_TOOLTIP);
			pluginDescriptionTextField[i] = getTextArea(
					Utility.getMessage(Resources.DROID64_SETTINGS_EXE_DESCR_TOOLTIP));
			forkThreadCheckBox[i] = new JCheckBox(Utility.EMPTY, true);
			forkThreadCheckBox[i].setToolTipText(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_FORK_TOOLTIP));
			
			var prg = Setting.getExternalProgram(i);
			if (prg != null) {
				pluginLabelTextField[i].setText(prg.getLabel());
				pluginCommandField[i].setPath(prg.getCommand());
				pluginArgumentTextField[i].setText(prg.getArguments());
				pluginDescriptionTextField[i].setText(prg.getDescription());
				forkThreadCheckBox[i].setSelected(prg.isForkThread());
			}

			GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, pluginPanel[i],
					new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_LABEL)));
			GuiHelper.addToGridBag(1, 0, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginLabelTextField[i]);

			GuiHelper.addToGridBag(0, 1, 0.0, 0.0, 1, gbc, pluginPanel[i],
					new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_COMMAND)));
			GuiHelper.addToGridBag(1, 1, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginCommandField[i]);

			GuiHelper.addToGridBag(0, 2, 0.0, 0.0, 1, gbc, pluginPanel[i],
					new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_ARGS)));
			GuiHelper.addToGridBag(1, 2, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginArgumentTextField[i]);

			GuiHelper.addToGridBag(0, 3, 0.0, 0.0, 1, gbc, pluginPanel[i],
					new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_DESCR)));
			GuiHelper.addToGridBag(1, 3, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginDescriptionTextField[i]);

			GuiHelper.addToGridBag(0, 4, 0.0, 0.0, 1, gbc, pluginPanel[i],
					new JLabel(Utility.getMessage(Resources.DROID64_SETTINGS_EXE_FORK)));
			GuiHelper.addToGridBag(1, 4, 0.5, 0.0, 1, gbc, pluginPanel[i], forkThreadCheckBox[i]);
	
			GuiHelper.addToGridBag(0, 5, 0.5, 1.0, 2, gbc, pluginPanel[i], new JPanel());
		}
		return pluginPanel;
	}

	private JTextArea getTextArea(String toolTip) {
		var area = new JTextArea(4, 20);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setToolTipText(toolTip);
		area.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		area.setText(Utility.EMPTY);
		return area;
	}

	private JTextField getTextField(String toolTipPropertyKey) {
		var field = new JTextField();
		field.setToolTipText(Utility.getMessage(toolTipPropertyKey));
		field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		field.setText(Utility.EMPTY);
		return field;
	}

	private static String getSqlSetupScript(final MainPanel mainPanel) {
		if (sqlSetupScript == null) {
			try (var in = SettingsPanel.class.getResourceAsStream("/setup_database.sql");
					var input = new BufferedReader(new InputStreamReader(in))) {
				var buf = new StringBuilder();
				for (var line = input.readLine(); line != null; line = input.readLine()) {
					buf.append(line);
					buf.append('\n');
				}
				sqlSetupScript = buf.toString();
			} catch (IOException e) { // NOSONAR
				mainPanel.appendConsole("Failed to find SQL setup script.\n" + e.getMessage());
			}
		}
		return sqlSetupScript;
	}
}
