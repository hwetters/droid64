package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

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
public class ViewImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final MainPanel mainPanel;
	private final ImagePanel imgPanel = new ImagePanel();
	private final String title;
	private final JButton closeButton = new JButton("Close");
	private JDialog dialog;
	private List<byte[]> dataList;
	private List<String> nameList;
	private int currentIndex = 0;

	public ViewImagePanel(String title, MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		this.title = title;
		this.dialog = new JDialog(mainPanel.getParent(), title, true);
		initGUI();
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void show(List<byte[]> dataList, List<String> nameList) throws IOException {
		this.dataList = dataList != null ? dataList : new ArrayList<>();
		this.nameList = nameList != null ? nameList : new ArrayList<>();
		currentIndex = 0;
		imgPanel.setImage(getNextImage());
		showImage();
	}

	public void show(Image img, String name) {
		imgPanel.setImage(img, name);
		showImage();
	}

	private void showImage() {
		closeButton.addActionListener(e -> dialog.dispose());

		dialog.setTitle(title);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		add(new JScrollPane(drawImagePanel()), BorderLayout.CENTER);

		var printButton = new JButton("Print");
		printButton.setMnemonic('p');
		printButton.setToolTipText("Print");
		printButton.addActionListener(ae -> print(imgPanel));

		var saveButton = new JButton("Save PNG");
		saveButton.setMnemonic('s');
		saveButton.addActionListener(ae -> saveImage(imgPanel));

		var buttonPanel = new JPanel();
		buttonPanel.add(printButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	private JPanel drawImagePanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		imgPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		imgPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickedImage();
			}
		});
		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, gbc, panel, imgPanel);
		return panel;
	}

	private void clickedImage() {
		try {
			imgPanel.setImage(getNextImage());
		} catch (IOException e) {
			mainPanel.appendConsole("Failed to get image: " + e.getMessage());
		}
	}

	private void saveImage(ImagePanel imgPanel) {
		try {
			var filename = FileDialogHelper.openTextFileDialog("Save PNG file", null, imgPanel.getName(), true, new String[]{ ".png" });
			if (filename != null) {
				ImageIO.write(imgPanel.getImage(), "png", new File(filename));
			}
		} catch (IOException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to save PNG image.\n"+e.getMessage());
		}
	}

	private CbmPicture getNextImage() {
		if (dataList == null || dataList.isEmpty()) {
			return null;
		}
		currentIndex = currentIndex % dataList.size();
		var data = dataList.get(currentIndex);
		String name = currentIndex < nameList.size() ? nameList.get(currentIndex) : null;
		var cbm = new CbmPicture(data, name);
		currentIndex++;
		return cbm;
	}

	private void print(ImagePanel imgPanel) {
		var job = PrinterJob.getPrinterJob();
		job.setPageable(new PrintPageable(imgPanel.getImage(), imgPanel.getName(), mainPanel));
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException e) {	//NOSONAR
				mainPanel.appendConsole("Failed to print image.\n"+e.getMessage());
			}
		}
	}

	protected void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}
}
