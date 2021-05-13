package droid64.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

class FilePathPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String BROWSELABEL = "...";
	private final JTextField path = new JTextField();
	private final JButton browseButton =  new JButton(BROWSELABEL);
	private final transient List<Consumer<File>> listeners = new ArrayList<>();
	private transient FileFilter fileFilter = null;

	/**
	 *
	 * @param defaultPath file with default path
	 * @param mode the browser mode (e.g. JFileChooser.DIRECTORIES_ONLY, FileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES)
	 * @param consumer add listener for selected path
	 */
	public FilePathPanel(File defaultPath, int mode, Consumer<File> consumer) {
		if (consumer !=null) {
			listeners.add(consumer);
		}
		setLayout(new BorderLayout());
		path.setText(defaultPath.getPath());
		add(path, BorderLayout.CENTER);
		add(browseButton, BorderLayout.EAST);
		browseButton.addActionListener(event -> {
			var choosen = openDialog(new File(path.getText()), mode);
			if (choosen != null) {
				path.setText(choosen.getPath());
				listeners.forEach(c -> c.accept(choosen));
			}
		});
	}

	/**
	 * @return currently selected path
	 */
	public String getPath() {
		return path.getText();
	}

	public void setPath(String path) {
		this.path.setText(path);
	}
	/**
	 * @param consumer add listener for selected path
	 */
	public void addListener(Consumer<File> consumer) {
		listeners.add(consumer);
	}

	public void setFileFilter(FileFilter filter) {
		this.fileFilter = filter;
	}

	@Override
	public void setToolTipText(String text) {
		path.setToolTipText(text);
	}

	/**
	 * Open a browser dialog.
	 * @param directory String with default path
	 * @param mode the browser mode (e.g. JFileChooser.DIRECTORIES_ONLY, FileChooser.FILES_ONLY or JFileChooser.FILES_AND_DIRECTORIES)
	 * @return File with selected path, or null if nothing was selected.
	 */
	private File openDialog(File directory, int mode) {
		var chooser = new JFileChooser(directory);
		chooser.setFileSelectionMode(mode);
		chooser.setMultiSelectionEnabled(false);
		if (fileFilter != null) {
			chooser.setFileFilter(fileFilter);
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
}
