package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import droid64.d64.CbmException;
import droid64.d64.Utility;

/**<pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-15
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
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
 *   http://droid64.sourceforge.net
 *
 * @author Henrik
 * </pre>
 */
public class TextViewPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final MainPanel mainPanel;
	private final JTextPane textPane = new JTextPane();
	private final JLabel titleLabel = new JLabel();
	private final JButton printButton = new JButton("Print");
	private final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
	private final JButton closeButton = new JButton("Close");
	private JDialog dialog;
	private String name;

	public TextViewPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		this.dialog = new JDialog(mainPanel.getParent(), "Text", true);
		setup();
	}

	public void show(byte[] data, String title, String name) {
		show(filter(data), title, name, Utility.MIMETYPE_TEXT);
	}

	public void show(String message, String title, String name, String mimeType) {
		this.name = name;
		textPane.setContentType(mimeType);
		textPane.setText(message);
		textPane.setCaretPosition(0);

		printButton.setVisible(!Utility.MIMETYPE_HTML.equals(mimeType));
		c64ModeButton.setVisible(!Utility.MIMETYPE_HTML.equals(mimeType));

		GuiHelper.setPreferredSize(this, 2, 2);
		closeButton.addActionListener(e -> dialog.dispose());

		dialog.setTitle(title + " - " + name);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}

	public void setModal(boolean modal) {
		dialog.setModal(modal);
	}

	private String filter(byte[] data) {
		if (data == null) {
			return "";
		}
		var filtered = new byte[data.length];
		int out = 0;
		for (var in=0; in<data.length; in++) {
			byte c = data[in];
			if ((c>=0x20 && c<=0x7e) || c==0x09 || c==0x0a || c== 0x0d || (c>=0xa0 && c<=0xff)) {
				filtered[out++] = c;
			}
		}
		return new String(Arrays.copyOfRange(filtered, 0, out), StandardCharsets.ISO_8859_1);
	}

	private void setTextFont(JComponent component, boolean useCbmFont) {
		Font font = useCbmFont ? Setting.CBM_FONT.getFont() : Setting.SYS_FONT.getFont();
		component.setFont(font);
	}

	private void setup() {

		textPane.setEditable(false);

		c64ModeButton.addActionListener(ae -> setTextFont(textPane, c64ModeButton.isSelected()));
		c64ModeButton.setMnemonic('c');

		var saveButton = new JButton("Save");
		saveButton.addActionListener(ae -> save(textPane.getText()));
		saveButton.setMnemonic('s');

		printButton.addActionListener(ae -> print(textPane.getText(), c64ModeButton.isSelected()));
		printButton.setMnemonic('p');

		var buttonPanel = new JPanel();
		buttonPanel.add(printButton);
		buttonPanel.add(c64ModeButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		var scrollPane = new JScrollPane(textPane);

		GuiHelper.keyNavigateTextArea(textPane, scrollPane);

		setLayout(new BorderLayout());
		add(titleLabel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		GuiHelper.setSize(this, 6, 2);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	private void save(String text) {
		if (text.isEmpty()) {
			return;
		}
		var saveFileName = FileDialogHelper.openTextFileDialog("Save text file", null, Utility.EMPTY, true, new String[] {".txt", ".asm"});
		if (saveFileName != null) {
			try {
				Utility.writeFile(new File(saveFileName), text);
			} catch (CbmException e) {
				mainPanel.appendConsole("Error: Failed to write to file " + saveFileName + '\n' + e);
			}
		}
	}

	private void print(final String text, boolean useCbmFont) {
		try {
			var	job = PrinterJob.getPrinterJob();
			job.setPageable(new PrintPageable(text, name, useCbmFont, false, mainPanel));
			if (job.printDialog()) {
				job.print();
			}
		} catch (PrinterException e) {
			mainPanel.appendConsole("Failed to print text.\n"+e);
		}
	}

	protected void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}
}
