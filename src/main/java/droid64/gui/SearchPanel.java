package droid64.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;
import droid64.db.DaoFactory;
import droid64.db.DatabaseException;
import droid64.db.DiskSearchCriteria;
import droid64.db.SearchResultRow;

/**
 * Search dialog for DroiD64
 *
 * @author Henrik
 */
public class SearchPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final MainPanel mainPanel;
	private final SearchResultTableModel tableModel = new SearchResultTableModel();

	private final JTextField fileNameText = new JTextField(Utility.EMPTY, 20);
	private final JTextField diskLabelText = new JTextField(Utility.EMPTY, 20);
	private final JTextField diskPathText = new JTextField(Utility.EMPTY, 20);
	private final JTextField diskFileNameText = new JTextField(Utility.EMPTY, 20);
	private final JTextField hostNameText = new JTextField(Utility.EMPTY, 20);
	private final JFormattedTextField fileSizeMinField = getNumericField(10, 8);
	private final JFormattedTextField fileSizeMaxField = getNumericField(250, 8);
	private final JButton closeButton = new JButton("Close");
	private final String title;

	/**
	 * Constructor
	 * @param title String
	 * @param mainPanel MainPanel
	 */
	public SearchPanel (String title, MainPanel mainPanel) {
		this.title = title;
		this.mainPanel = mainPanel;
		setLayout(new BorderLayout());
		add(drawSearchPanel(), BorderLayout.CENTER);

		GuiHelper.setPreferredSize(this, 2, 2);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	/**
	 * Open selected row from search result table into the specified disk panel.
	 * @param diskPanel DiskPanel where a disk image will be opened.
	 * @param table JTable containing the search results and where one row could be selected by user.
	 */
	private void openSelected(DiskPanel diskPanel, JTable table) {
		int row = table.getSelectedRow();
		if (row >= 0) {
			String path = (String) tableModel.getValueAt(row, 0);
			String file = (String) tableModel.getValueAt(row, 1);
			diskPanel.openDiskImage(new File(path + File.separator + file), true);
		}
	}

	public static JFormattedTextField getNumericField(int initValue, int columns) {
		var dec = new DecimalFormat();
		dec.setGroupingUsed(false);
		dec.setMaximumIntegerDigits(columns);
		var field = new JFormattedTextField(dec);
		field.setText(Integer.toString(initValue));
		field.setColumns(columns);
		return field;
	}

	/** Setup the search panel */
	private JPanel drawSearchPanel() {

		var fileSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileSizePanel.add(fileSizeMinField);
		fileSizePanel.add(new JLabel(" - "));
		fileSizePanel.add(fileSizeMaxField);

		var fileTypeBox = new JComboBox<FileType>(FileType.values());
		fileTypeBox.insertItemAt(null, 0);
		fileTypeBox.setSelectedIndex(0);
		var fileTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileTypePanel.add(fileTypeBox);

		var imageTypeBox = new JComboBox<>(DiskImageType.values());
		imageTypeBox.setSelectedIndex(0);
		var imageTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		imageTypePanel.add(imageTypeBox);

		// Search result table
		var columnModel = new DefaultTableColumnModel();
		for (var i=0; i<tableModel.getColumnCount(); i++) {
			TableColumn col;
			switch (i) {
			case 2:
			case 3:
				col = new TableColumn(i, 100);
				break;
			case 4:
			case 5:
				col = new TableColumn(i,  20);
				break;
			default:
				col = new TableColumn(i, 80);
				break;
			}
			col.setHeaderValue(tableModel.getColumnHeader(i));
			columnModel.addColumn(col);
		}
		var table = new JTable(tableModel, columnModel);
		// Buttons
		var searchButton = new JButton(Utility.getMessage(Resources.DROID64_SEARCH_SEARCH));
		var openSelectedLeftButton = new JButton(Utility.getMessage(Resources.DROID64_SEARCH_OPENLEFT));
		var openSelectedRightButton = new JButton(Utility.getMessage(Resources.DROID64_SEARCH_OPENRIGHT));
		openSelectedLeftButton.setEnabled(false);
		openSelectedRightButton.setEnabled(false);

		searchButton.addActionListener(ae -> search((FileType) fileTypeBox.getSelectedItem(), (DiskImageType) imageTypeBox.getSelectedItem()));
		openSelectedLeftButton.addActionListener(ae -> openSelected(mainPanel.getLeftDiskPanel(), table));
		openSelectedRightButton.addActionListener(ae -> openSelected(mainPanel.getRightDiskPanel(), table));
		// Button panel
		var buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(openSelectedLeftButton);
		buttonPanel.add(openSelectedRightButton);
		buttonPanel.add(closeButton);

		table.getSelectionModel().addListSelectionListener(e -> {
			boolean selected = table.getSelectedRows().length > 0;
			openSelectedLeftButton.setEnabled(selected);
			openSelectedRightButton.setEnabled(selected);
		});

		var tableScrollPane = new JScrollPane( table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		GuiHelper.keyNavigateTable(table);

		// Title
		var titleLabel = new JLabel(Utility.getMessage(Resources.DROID64_SEARCH_SEARCH));
		titleLabel.setFont(new Font("Verdana",  Font.BOLD, titleLabel.getFont().getSize() * 2));

		// Put widgets onto panel
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;

		var panel = new JPanel(new GridBagLayout());
		panel.add(titleLabel, gbc);

		addComponent(1, Resources.DROID64_SEARCH_FILENAME, panel, fileNameText, gbc);
		addComponent(2, Resources.DROID64_SEARCH_FILESIZE, panel, fileSizePanel, gbc);
		addComponent(3, Resources.DROID64_SEARCH_FILETYPE, panel, fileTypePanel, gbc);
		addComponent(4, Resources.DROID64_SEARCH_IMAGETYPE, panel, imageTypePanel, gbc);
		addComponent(5, Resources.DROID64_SEARCH_IMAGELABEL, panel, diskLabelText, gbc);
		addComponent(6, Resources.DROID64_SEARCH_IMAGEPATH, panel, diskPathText, gbc);
		addComponent(7, Resources.DROID64_SEARCH_IMAGEFILE, panel, diskFileNameText, gbc);
		addComponent(8, Resources.DROID64_SEARCH_HOSTNAME, panel, hostNameText, gbc);
		gbc.weighty = 1.0;
		addComponent(9, Utility.EMPTY, panel, tableScrollPane, gbc);
		gbc.weighty = 0.0;
		addComponent(10, Utility.EMPTY, panel, buttonPanel, gbc);
		return panel;
	}

	private void addComponent(int row, String propKey, JPanel parent, JComponent component, GridBagConstraints gbc) {
		GuiHelper.addToGridBag(0, row, 0.0, gbc, parent, new JLabel(Utility.getMessage(propKey)));
		GuiHelper.addToGridBag(1, row, 0.5, gbc, parent, component);
	}

	protected void search(FileType selectedFileType, DiskImageType imageType ) {
		var criteria = new DiskSearchCriteria();
		criteria.setFileName(fileNameText.getText());
		criteria.setDiskLabel(diskLabelText.getText());
		criteria.setDiskPath(diskPathText.getText());
		criteria.setDiskFileName(diskFileNameText.getText());
		criteria.setFileSizeMin(Utility.parseInteger(fileSizeMinField.getText(), 10));
		criteria.setFileSizeMax(Utility.parseInteger(fileSizeMaxField.getText(), 250));
		criteria.setHostName(hostNameText.getText());
		criteria.setFileType(selectedFileType);
		criteria.setImageType(imageType != null && imageType != DiskImageType.UNDEFINED ? imageType : null);
		runSearch(criteria);
	}

	/**
	 * Perform search using criteria. Update table model with search results.
	 * @param criteria DiskSearchCriteria
	 */
	private void runSearch(DiskSearchCriteria criteria) {
		try {
			tableModel.clear();
			DaoFactory.getDaoFactory().getDiskDao()
				.search(criteria)
				.map(disk -> new SearchResultRow(disk, disk.getDiskFiles().get(0)))
				.forEach(tableModel::updateDirEntry);
		} catch (DatabaseException e) {	//NOSONAR
			if (mainPanel != null) {
				mainPanel.appendConsole(e.getMessage());
			}
		}
	}

	public void showDialog() {
		var dialog = new JDialog(mainPanel.getParent(), title, true);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		closeButton.addActionListener(e -> dialog.dispose());
		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}
}
