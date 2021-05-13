package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import droid64.DroiD64;
import droid64.d64.Utility;

/**<pre style='font-family:sans-serif;'>
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
public class BugsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static transient String todo = null;

	protected BugsPanel () {
		var bugsPanel = createTextPane(Utility.getMessage(Resources.DROID64_BUGS_INGRESS), Resources.DROID64_BUGS_BUGS, BorderLayout.SOUTH);
		var todoPanel = createTextPane(getBugs(), Resources.DROID64_BUGS_TODO, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(bugsPanel, BorderLayout.NORTH);
		add(todoPanel, BorderLayout.CENTER);

		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void showDialog(Component parent) {
		JOptionPane.showMessageDialog(parent, this, DroiD64.PROGNAME+ " - Bugs and ToDo", JOptionPane.PLAIN_MESSAGE);
	}

	private JPanel createTextPane(String message, String labelPropKey, String constraints) {
		var bugsPanel = new JPanel(new BorderLayout());
		bugsPanel.add(new JLabel(Utility.getMessage(labelPropKey)), BorderLayout.NORTH);
		bugsPanel.add(new JScrollPane(drawTextArea(message)), constraints);
		return bugsPanel;
	}

	private JTextPane drawTextArea(String message) {
		var textArea = new JTextPane();
		textArea.setContentType(Utility.MIMETYPE_HTML);
		textArea.setEditable(false);
		textArea.setText(message);
		textArea.setCaretPosition(0);
		return textArea;
	}

	protected static String getBugs() {
		if (todo == null) {
			todo = Utility.getResource("resources/bugs.html");
		}
		return todo;
	}
}
