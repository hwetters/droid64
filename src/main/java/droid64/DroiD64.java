package droid64;

import javax.swing.JFrame;

import droid64.d64.Utility;
import droid64.gui.MainPanel;
import droid64.gui.Resources;
import droid64.gui.Setting;

/**
 * <pre style='font-family:sans-serif;'>
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
 * </pre>
 *
 * @author wolf
 */
public class DroiD64 {

	public static final String PROGNAME = "droiD64";
	public static final String TITLE = "Alpha Version Warning: MAY HAVE ERRORS! USE ONLY ON BACKUPS! LOOK AT \"BUGS AND TO-DO\"!";
	public static final String VERSION = Utility.getMessage(Resources.DROID_VERSION);

	private DroiD64() {
		super();
	}

	public static void main(String[] args) {
		Setting.load();
		JFrame mainFrame = new JFrame();
		new MainPanel(mainFrame);
		mainFrame.setVisible(true);
	}
}
