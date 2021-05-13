package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import droid64.DroiD64;
import droid64.d64.Utility;

/**<pre style='font-family:Sans,Arial,Helvetica'>
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
 *</pre>
 * @author wolf
 */
public class ShowHelpPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static String about = null;

	/**
	 * Constructor
	 */
	protected ShowHelpPanel () {

		setLayout(new BorderLayout());

		var imageIcon = new ImageIcon(getClass().getResource("resources/wolf.jpg"));
		imageIcon.setDescription("Me having some breakfast.");
		var imagePanel = new JPanel();
		imagePanel.add(new JLabel(imageIcon, SwingConstants.CENTER), BorderLayout.CENTER);
		imagePanel.setToolTipText("Me having some breakfast.");

		var messageTextArea = new JTextPane();
		messageTextArea.setContentType(Utility.MIMETYPE_HTML);
		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setText(getAbout());
		messageTextArea.setCaretPosition(0);

		add(imagePanel, BorderLayout.NORTH);
		add(new JScrollPane(messageTextArea), BorderLayout.CENTER);

		GuiHelper.setPreferredSize(this, 3, 2);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void showDialog(Component parent) {
		JOptionPane.showMessageDialog(parent, this, DroiD64.PROGNAME + " - About", JOptionPane.PLAIN_MESSAGE);
	}

	protected static String getAbout() {
		if (about == null) {
			var str = Utility.getResource("resources/about.html");
			about = MessageFormat.format(str,
					System.getProperty("java.vendor"),
					System.getProperty("java.version"),
					System.getProperty("os.name"),
					System.getProperty("os.version"),
					System.getProperty("os.arch"));
		}
		return about;
	}
}
