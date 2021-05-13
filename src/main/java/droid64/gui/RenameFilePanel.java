package droid64.gui;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import droid64.d64.CbmException;
import droid64.d64.CbmFile;
import droid64.d64.FileType;

/*
 * Created on 26.06.2004
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
 */

/**
 * @author wolf
 */
public class RenameFilePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField oldNameTextField = new JTextField(16);
	private final JTextField newNameTextField = new JTextField(16);
	private final JTextField cbmNameTextField = new JTextField(16);
	private final JTextField idField = new JTextField(5);
	private final JTextField cbmIdTextField = new JTextField(5);
	private final JComboBox<FileType> fileTypeBox = new JComboBox<>();
	private final LimitLengthDocument nameLengthDocument = new LimitLengthDocument();
	private final LimitLengthDocument idLengthDocument = new LimitLengthDocument();
	private final JLabel oldNameLabel = new JLabel("Old name:");
	private final JLabel sizeLabel = new JLabel("Size:");
	private final JLabel fileTypeLabel = new JLabel("File type:");
	private final JFormattedTextField sizeField = GuiHelper.getNumField(0, Integer.MAX_VALUE, 0, 4);
	private final Component parentComponent;

	/**
	 * Constructor
	 * @param parentComponent parentComponent
	 * @throws CbmException in case of error
	 */
	public RenameFilePanel (Component parentComponent) throws CbmException {
		this.parentComponent = parentComponent;
		initGUI();
	}

	/**
	 * Show dialog for renaming this local file
	 * @param title title
	 * @param file file
	 * @return RenameResult or null if cancelled
	 */
	public RenameResult show (String title, File file) {
		return show(title, file.getName(), null, false, false);
	}

	/**
	 * Show dialog for renaming this file on an image
	 * @param title title
	 * @param cbmFile cbmFile
	 * @return RenameResult or null if cancelled
	 */
	public RenameResult show (String title, CbmFile cbmFile) {
		sizeField.setText(Integer.toString(cbmFile.getSizeInBlocks()));
		return show(title, cbmFile.getName(), cbmFile.getFileType(), true, false);
	}

	/**
	 * Show as dialog
	 * @param title title
	 * @param fileName file name
	 * @param fileType file type
	 * @param isCbmFile true if it is a CBM file
	 * @param createPartition true if it is a partition (CBM) file type
	 * @return a RenameResult or null if cancelled
	 */
	public RenameResult show(String title, String fileName, FileType fileType, boolean isCbmFile, boolean createPartition) {

		nameLengthDocument.setText(fileName);
		nameLengthDocument.setLimit(isCbmFile ? 16 : Integer.MAX_VALUE);

		idLengthDocument.setLimit(5);

		newNameTextField.setText(fileName);

		oldNameTextField.setText(fileName);
		oldNameLabel.setVisible(fileName != null);
		oldNameTextField.setVisible(fileName != null);

		cbmNameTextField.setText(fileName);
		cbmNameTextField.setVisible(isCbmFile);

		cbmIdTextField.setVisible(createPartition);

		fileTypeBox.setVisible(isCbmFile);
		fileTypeBox.setEnabled(isCbmFile && !createPartition && !FileType.CBM.equals(fileType));
		fileTypeLabel.setVisible(isCbmFile && !createPartition);

		idField.setVisible(createPartition);
		idField.setEditable(createPartition);

		sizeField.setEditable(createPartition);
		sizeField.setVisible(createPartition);
		sizeLabel.setVisible(createPartition);

		if (fileType != null) {
			fileTypeBox.setSelectedItem(fileType);
		}

		newNameTextField.setRequestFocusEnabled(true);

		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parentComponent, this, title, JOptionPane.OK_CANCEL_OPTION)) {
			return getResult();
		}
		return null;
	}

	private RenameResult getResult() {
		var result = new RenameResult();
		result.setFileName(newNameTextField.getText());
		if (fileTypeBox.getSelectedIndex() != -1) {
			result.setFileType((FileType) fileTypeBox.getSelectedItem());
		}
		result.setPartitionSectorCount((int) sizeField.getValue());
		result.setDiskID(idField.getText());
		return result;
	}

	private void initGUI() {

		fileTypeBox.setToolTipText("Select a filetype here.");
		for (var ft : FileType.values()) {
			fileTypeBox.addItem(ft);
		}

		oldNameTextField.setEditable(false);

		cbmNameTextField.setEditable(false);
		cbmNameTextField.setFont(Setting.CBM_FONT.getFont());
		cbmNameTextField.setBackground(Setting.DIR_BG.getColor());
		cbmNameTextField.setForeground(Setting.DIR_FG.getColor());
		cbmNameTextField.setBorder(BorderFactory.createCompoundBorder(cbmNameTextField.getBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		cbmIdTextField.setEditable(false);
		cbmIdTextField.setFont(Setting.CBM_FONT.getFont());
		cbmIdTextField.setBackground(Setting.DIR_BG.getColor());
		cbmIdTextField.setForeground(Setting.DIR_FG.getColor());
		cbmIdTextField.setBorder(BorderFactory.createCompoundBorder(cbmIdTextField.getBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		newNameTextField.setEditable(true);
		newNameTextField.setToolTipText("Enter the new filename here.");
		newNameTextField.setDocument(nameLengthDocument);
		newNameTextField.getDocument().addDocumentListener(new RenameDocumentListener(cbmNameTextField));

		idField.setDocument(idLengthDocument);
		idField.getDocument().addDocumentListener(new RenameDocumentListener(cbmIdTextField));

		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, this, oldNameLabel);
		GuiHelper.addToGridBag(1, 0, 1.0, 0.0, 1, gbc, this, oldNameTextField);
		GuiHelper.addToGridBag(2, 0, 0.0, 0.0, 1, gbc, this, new JPanel());

		GuiHelper.addToGridBag(0, 1, 0.0, 0.0, 1, gbc, this, new JLabel("New name:"));
		GuiHelper.addToGridBag(1, 1, 0.0, 0.0, 1, gbc, this, newNameTextField);
		GuiHelper.addToGridBag(2, 1, 1.0, 0.0, 1, gbc, this, idField);

		GuiHelper.addToGridBag(0, 2, 0.0, 0.0, 1, gbc, this, new JPanel());
		GuiHelper.addToGridBag(1, 2, 1.0, 0.0, 1, gbc, this, cbmNameTextField);
		GuiHelper.addToGridBag(2, 2, 0.0, 0.0, 1, gbc, this, cbmIdTextField);

		GuiHelper.addToGridBag(0, 3, 0.0, 0.0, 1, gbc, this, sizeLabel);
		GuiHelper.addToGridBag(1, 3, 0.0, 0.0, 1, gbc, this, sizeField);
		GuiHelper.addToGridBag(2, 3, 1.0, 0.0, 1, gbc, this, new JPanel());

		GuiHelper.addToGridBag(0, 4, 0.0, 0.0, 1, gbc, this, fileTypeLabel);
		GuiHelper.addToGridBag(1, 4, 0.0, 0.0, 1, gbc, this, fileTypeBox);
		GuiHelper.addToGridBag(2, 4, 1.0, 0.0, 1, gbc, this, new JPanel());

		GuiHelper.addToGridBag(0, 5, 1.0, 1.0, 3, gbc, this, new JPanel());

		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	/**
	 * RenameDocumentListener
	 */
	private class RenameDocumentListener implements DocumentListener {
		private final JTextField field;

		public RenameDocumentListener(final JTextField field) {
			this.field = field;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateField(e, field);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateField(e, field);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateField(e, field);
		}

		private void updateField(DocumentEvent e, JTextField field) {
			try {
				if (field.isVisible()) {
					var originator = e.getDocument();
					var text = e.getDocument().getText(0, originator.getLength());
					field.setText(text);
				}
			} catch (BadLocationException ignore) { /* ignore */ }
		}
	}
}
