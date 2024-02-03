package droid64.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import droid64.DroiD64;
import droid64.cfg.Bookmark;
import droid64.cfg.BookmarkType;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.Utility;

public class BookmarkPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Pair DEFAULT = new Pair(-1, "<none>");

	private final JTextField name = new JTextField();
	private final JTextField path = new JTextField();
	private final JComboBox<BookmarkType> type = new JComboBox<>();
	private final JTextField created = new JTextField();
	private final JComboBox<Pair> pluginNo = new JComboBox<>();
	private final JComboBox<Pair> selectedFile = new JComboBox<>();

	private final JTextField extArguments = new JTextField();
	private final JTextPane notes = new JTextPane();

	private final Component parentComponent;
	private final Bookmark bookmark;
	private final transient ConsoleStream consoleStream;

	public BookmarkPanel(Component parentComponent, Bookmark bookmark, ConsoleStream consoleStream) {
		this.consoleStream = consoleStream;
		this.parentComponent = parentComponent;
		this.bookmark = bookmark;
		setup();
	}

	private void enabledFields(BookmarkType bt) {
		name.setEnabled(bt != null && !BookmarkType.SEPARATOR.equals(bt));
		type.setEnabled(false);
		path.setEnabled(Utility.isOneOf(bt, BookmarkType.DISKIMAGE, BookmarkType.DIRECTORY));
		created.setEnabled(false);
		selectedFile.setEnabled(Utility.isOneOf(bt, BookmarkType.DISKIMAGE));
		pluginNo.setEnabled(Utility.isOneOf(bt, BookmarkType.DISKIMAGE, BookmarkType.DIRECTORY));
		extArguments.setEnabled(Utility.isOneOf(bt, BookmarkType.DISKIMAGE, BookmarkType.DIRECTORY));
		notes.setEditable(true);
	}

	public void showDialog() {
		enabledFields(bookmark.getBookmarkType());
		name.setText(bookmark.getName());
		path.setText(bookmark.getPath());
		type.setSelectedItem(bookmark.getBookmarkType());
		created.setText("" + bookmark.getCreated());
		extArguments.setText(bookmark.getExtArguments());
		notes.setText(bookmark.getNotes());

		GuiHelper.setSelected(pluginNo, b -> b.getNum() == bookmark.getPluginNo());

		if (BookmarkType.DISKIMAGE.equals(bookmark.getBookmarkType())) {
			try {
				var f = new File(bookmark.getPath());
				if (f.exists()) {
					var img = DiskImage.getDiskImage(f, consoleStream);
					img.readDirectory();

					selectedFile.removeAllItems();
					selectedFile.addItem(DEFAULT);
					img.getFileEntries().map(cf -> new Pair(cf.getDirPosition(), cf.getName()))
							.forEach(selectedFile::addItem);

					GuiHelper.setSelected(selectedFile, b -> b.getNum() == bookmark.getSelectedNo());
				}
			} catch (CbmException ex) {
				GuiHelper.showErrorMessage(parentComponent, "Failed to open disk image", ex.getMessage());
			}
		} else if (BookmarkType.SEPARATOR.equals(bookmark.getBookmarkType())) {
			return;
		}

		JOptionPane.showMessageDialog(parentComponent, this, DroiD64.PROGNAME + " - Bookmark", JOptionPane.PLAIN_MESSAGE);
		bookmark.setName(name.getText());
		bookmark.setPluginNo(Optional.ofNullable((Pair) pluginNo.getSelectedItem()).map(Pair::getNum).orElse(-1));
		bookmark.setSelectedNo(Optional.ofNullable((Pair) selectedFile.getSelectedItem()).map(Pair::getNum).orElse(-1));
		bookmark.setExtArguments(extArguments.getText());
		bookmark.setNotes(notes.getText());
	}

	private void setup() {
		name.setEditable(true);
		type.setEditable(false);
		type.setEnabled(false);
		created.setEnabled(false);

		notes.setContentType(Utility.MIMETYPE_TEXT);
		var notesScrollPane = new JScrollPane(notes);
		GuiHelper.keyNavigateTextArea(notes, notesScrollPane);
		notes.setCaretPosition(0);

		selectedFile.setToolTipText("The file on the image to be selected when bookmark has been opened");

		for (var bt : BookmarkType.values()) {
			type.addItem(bt);
		}

		var epList = Setting.getExternalPrograms();
		pluginNo.addItem(DEFAULT);
		pluginNo.setToolTipText("Plugin to execute when bookmark has been opened");
		for (var i = 0; i < epList.size(); i++) {
			pluginNo.addItem(new Pair(i, epList.get(i).getLabel()));
		}

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int r = 0;

		GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Name"));
		GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, name);
		r++;
		GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Type"));
		GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, type);
		r++;
		if (Utility.isOneOf(bookmark.getBookmarkType(), BookmarkType.DISKIMAGE, BookmarkType.DIRECTORY)) {
			GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Path"));
			GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, path);
			r++;
		}
		GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Created"));
		GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, created);
		r++;
		if (Utility.isOneOf(bookmark.getBookmarkType(), BookmarkType.DISKIMAGE, BookmarkType.DIRECTORY)) {
			GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Plugin"));
			GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, pluginNo);
			r++;
			GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Selected File"));
			GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, selectedFile);
			r++;
			GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Plugin arguments"));
			GuiHelper.addToGridBag(1, r, 1.0, 0.0, 1, gbc, this, extArguments);
			r++;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
			GuiHelper.addToGridBag(0, r, 0.0, 0.0, 1, gbc, this, new JLabel("Notes"));
	        gbc.fill = GridBagConstraints.BOTH;
			GuiHelper.addToGridBag(1, r, 1.0, 1.0, 1, gbc, this, notesScrollPane);
		} else {
			GuiHelper.addToGridBag(0, r, 1.0, 1.0, 2, gbc, this, new JPanel());
		}
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
		GuiHelper.setPreferredSize(this, 4, 4);
	}
}
