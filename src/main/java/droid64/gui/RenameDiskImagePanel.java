package droid64.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import droid64.d64.DiskImage;
import droid64.d64.DiskImageType;
import droid64.d64.Utility;

/*
 * Created on 25.06.2004
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
public class RenameDiskImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField nameTextField = new JTextField(Utility.EMPTY, 16);
	private final JTextField idTextField = new JTextField(Utility.EMPTY, 5);
	private final JCheckBox compressedBox = new JCheckBox("Compressed image", false);
	private final JCheckBox cpmBox = new JCheckBox("CP/M formatted", false);
	private final JComboBox<DiskImageType> diskTypeBox = new JComboBox<>(DiskImageType.stream().sorted((a,b)-> a.id.compareTo(b.id)).toArray(DiskImageType[]::new));
	private final MainPanel mainPanel;
	private boolean isNewImage;

	public RenameDiskImagePanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		initGUI();
	}

	/**
	 * Get disk image details for a new image to be created
	 * @param title title
	 * @return RenameResult or null if cancelled
	 */
	public RenameResult showDialog(String title) {
		return showDialog(title, null);
	}

	/**
	 * Get disk image details for an existing image to be changed
	 * @param title title
	 * @param diskImage the image to be changed
	 * @return RenameResult or null if cancelled
	 */
	public RenameResult showDialog(String title, DiskImage diskImage) {

		isNewImage = diskImage == null;
		if (isNewImage) {
			nameTextField.setText("");
			idTextField.setText("");
			cpmBox.setSelected(false);
			compressedBox.setSelected(false);
			diskTypeBox.setSelectedItem(null);
		} else {
			nameTextField.setText(diskImage.getBam().getDiskName());
			idTextField.setText(diskImage.getBam().getDiskId());
			cpmBox.setSelected(diskImage.isCpmImage());
			compressedBox.setSelected(diskImage.isCompressed());
			diskTypeBox.setSelectedItem(DiskImageType.stream().filter(dt -> dt == diskImage.getDiskImageType()).findFirst().orElse(null));
		}

		cpmBox.setEnabled(isNewImage);
		compressedBox.setEnabled(isNewImage);
		diskTypeBox.setEnabled(isNewImage);

		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(mainPanel.getParent(), this, title,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null)) {
			return getResult();
		}
		return null;
	}

	private RenameResult getResult() {
		var diskId = Utility.truncate(idTextField.getText(), 5);
		var diskType = (DiskImageType) diskTypeBox.getSelectedItem();
		if (diskId.length() <= 2) {
			var dosVer = diskType != null ? diskType.dosVersion : "  ";
			diskId = (diskId + "  ").substring(0, 2) + "\u00a0" + dosVer;
		}
		var result = new RenameResult();
		result.setDiskName(Utility.truncate(nameTextField.getText(), 16));
		result.setDiskID(diskId);
		if (isNewImage) {
			result.setDiskType(diskType);
			result.setCpmDisk(cpmBox.isSelected());
			result.setCompressedDisk(compressedBox.isSelected());
		}
		return result;
	}


	private void initGUI() {

		nameTextField.setToolTipText("The label of your image (max 16 characters)");
		nameTextField.setDocument(new LimitLengthDocument(16, ""));

		idTextField.setToolTipText("The disk ID of your image.");
		idTextField.setDocument(new LimitLengthDocument(5, ""));
		idTextField.setToolTipText("The disk ID (max 5 characters)");

		diskTypeBox.setToolTipText("Select a disktype.");
		diskTypeBox.setEditable(false);
		diskTypeBox.setSelectedIndex(0);

		compressedBox.setToolTipText("GZIP new image.");

		cpmBox.setToolTipText("Format for CP/M.");

		final var nameTextField2 = new JTextField(Utility.EMPTY, 16);
		nameTextField2.setBackground(Setting.DIR_BG.getColor());
		nameTextField2.setForeground(Setting.DIR_FG.getColor());
		nameTextField2.setEditable(false);
		nameTextField2.setText("");
		nameTextField2.setBorder(BorderFactory.createCompoundBorder(nameTextField2.getBorder(),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		nameTextField.getDocument().addDocumentListener(new MyDocumentListener(nameTextField2));

		final var idTextField2 = new JTextField(Utility.EMPTY, 5);
		idTextField2.setBackground(Setting.DIR_BG.getColor());
		idTextField2.setForeground(Setting.DIR_FG.getColor());
		idTextField2.setEditable(false);
		idTextField2.setText("");
		idTextField2.setBorder(BorderFactory.createCompoundBorder(idTextField2.getBorder(),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		idTextField.getDocument().addDocumentListener(new MyDocumentListener(idTextField2));

		Font cbmFont = Setting.CBM_FONT.getFont();
		nameTextField2.setFont(cbmFont);
		idTextField2.setFont(cbmFont);

		setLayout(new GridBagLayout());

		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		int row = 0;
		var createPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		createPanel.add(compressedBox);
		createPanel.add(cpmBox);

		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, this, new JLabel("Image Type:"));
		GuiHelper.addToGridBag(1, row, 0.0, 0.0, 2, gbc, this, diskTypeBox);
		row++;
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, this, new JPanel());
		GuiHelper.addToGridBag(1, row, 0.0, 0.0, 2, gbc, this, createPanel);
		row++;
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, this, new JLabel("Disk Name:"));
		GuiHelper.addToGridBag(1, row, 1.0, 0.0, 1, gbc, this, nameTextField);
		GuiHelper.addToGridBag(2, row, 0.0, 0.0, 1, gbc, this, idTextField);
		row++;
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, this, new JLabel(Utility.EMPTY));
		GuiHelper.addToGridBag(1, row, 1.0, 0.0, 1, gbc, this, nameTextField2);
		GuiHelper.addToGridBag(2, row, 0.0, 0.0, 1, gbc, this, idTextField2);
		row++;
		GuiHelper.addToGridBag(0, row, 1.0, 1.0, 3, gbc, this, new JPanel());

		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	private class MyDocumentListener implements DocumentListener {

		private final JTextComponent receiver;

		public MyDocumentListener(JTextComponent receiver) {
			this.receiver = receiver;
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateField(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateField(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateField(e);
		}

		private void updateField(DocumentEvent e) {
			try {
				String text = e.getDocument().getText(0, e.getDocument().getLength());
				receiver.setText(text);
			} catch (BadLocationException ignore) { /* ignored */ }
		}
	}
}
