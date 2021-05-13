package droid64.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import droid64.DroiD64;
import droid64.d64.DirEntry;
import droid64.d64.Utility;
import droid64.db.Bookmark;
import droid64.db.Bookmark.BookmarkType;
import droid64.db.BookmarkList;

/**
 * BookmarkTree class
 */
public final class BookmarkTree extends JTree {

	private static final long serialVersionUID = 1L;
	private static final Class<BookmarkList> CLAZZ = BookmarkList.class;

	private static final Icon DISKIMAGE_ICON = MetalIconFactory.getTreeFloppyDriveIcon();
	private static final Icon DIRECTORY_ICON = MetalIconFactory.getTreeFolderIcon();

	private DefaultTreeModel model = null;
	private BookmarkList bookmarkList = null;
	private List<JComponent> menuItems = new ArrayList<>();
	private List<JButton> buttons = new ArrayList<>();
	private final MainPanel mainPanel;
	private File file = null;
	private final transient ConsoleStream consoleStream;

	/**
	 * Constructor
	 * @param parent the main panel
	 * @param consoleStream the stream for errors
	 */
	public BookmarkTree(MainPanel parent, ConsoleStream consoleStream) {
		this.mainPanel = parent;
		this.consoleStream = consoleStream;
		setup();
	}

	/**
	 * @param file the file to read BookmarkList XML from
	 */
	public void load(File file) {
		this.file = file;
		menuItems.clear();
		if (!file.exists()) {
			mainPanel.appendConsole("No bookmark file exists.");
			bookmarkList = new BookmarkList();
			var root = new DefaultMutableTreeNode(bookmarkList);
			model = new DefaultTreeModel(root);
			setModel(model);
			return;
		}
		try {
			bookmarkList = JAXB.unmarshal(file, CLAZZ);
			var root = new DefaultMutableTreeNode(bookmarkList);
			loadTree(root, null, bookmarkList.getBookmarks());
			model = new DefaultTreeModel(root);
			setModel(model);
		} catch (Exception ex) {
			GuiHelper.showException(this, "Error", ex, "Failed reading bookmark file");
		}
	}

	public List<JComponent> getMenuItems() {
		return menuItems;
	}

	/**
	 * Show the loaded BookmarkList XML in a dialog
	 */
	public void showTree() {
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
		JOptionPane.showMessageDialog(mainPanel.getParent(), new JScrollPane(this),	DroiD64.PROGNAME + " - Manage Bookmarks", JOptionPane.PLAIN_MESSAGE);
		save();
	}

	/**
	 * @param bookmark     the bookmark to add
	 * @param selectedNode the selected node to add the bookmark after. if null add
	 *                     last.
	 * @return the new menu item
	 */
	public JMenuItem addEntry(Bookmark bookmark, DefaultMutableTreeNode selectedNode) {
		var mi = new JMenuItem(bookmark.getName());
		menuItems.add(mi);
		bookmarkList.getBookmarks().add(bookmark);
		save(bookmarkList, file);

		var parent = Optional.ofNullable(selectedNode).map(DefaultMutableTreeNode::getParent)
				.map(DefaultMutableTreeNode.class::cast).orElse((DefaultMutableTreeNode) model.getRoot());
		var newNode = new DefaultMutableTreeNode(bookmark);
		if (selectedNode != null) {
			model.insertNodeInto(newNode, parent, parent.getIndex(selectedNode) + 1);
		} else {
			parent.add(newNode);
		}

		model.reload();

		mi.addActionListener(e -> {
			var dp = mainPanel.getActiveDiskPanel();
			if (dp != null) {
				dp.doubleClickedLocalfile(new File(bookmark.getPath()), 0);
			}
		});
		return mi;
	}

	public List<JButton> getButtons() {
		return buttons;
	}

	/**
	 * Setup GUI
	 */
	private void setup() {
		setDragEnabled(true);
		setDropMode(DropMode.ON_OR_INSERT);
		setTransferHandler(new TreeTransferHandler(mainPanel));
		getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		setRootVisible(false);
		setShowsRootHandles(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					var node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
					if (node != null) {
						show(node.getUserObject());
					}
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					var node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
					createPopupMenu(node).show(BookmarkTree.this, e.getX(), e.getY());
				}
			}
		});

		setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				var node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof Bookmark) {
					var b = (Bookmark) node.getUserObject();
					switch (b.getBookmarkType()) {
					case SEPARATOR:
						return getLabel(Bookmark.SEPARATOR_STRING, null, sel);
					case DISKIMAGE:
						return getLabel(b.getName(), DISKIMAGE_ICON, sel);
					case DIRECTORY:
						return getLabel(b.getName(), DIRECTORY_ICON, sel);
					default:
					}
				}
				return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			}

			private JLabel getLabel(String name, Icon icon, boolean selected) {
				var lbl = new JLabel(name, icon, SwingConstants.LEFT);
				lbl.setOpaque(true);
				lbl.setForeground(selected ? getTextSelectionColor() : getTextNonSelectionColor());
				lbl.setBackground(selected ? getBackgroundSelectionColor() : getBackgroundNonSelectionColor());
				var fnt = lbl.getFont();
				lbl.setFont(fnt.deriveFont(fnt.getStyle() & ~Font.BOLD));
				return lbl;
			}
		});
	}

	/**
	 * @param selectedObject the object selected in the tree
	 * @return the popup menu for the selected object
	 */
	private JPopupMenu createPopupMenu(DefaultMutableTreeNode selectedNode) {
		var popup = new JPopupMenu();
		var selectedBookmark = Optional.ofNullable(selectedNode).map(DefaultMutableTreeNode::getUserObject)
				.filter(Bookmark.class::isInstance).map(Bookmark.class::cast).orElse(null);

		if (selectedBookmark != null) {
			popup.add(new JMenuItem("Edit...")).addActionListener(e -> show(selectedBookmark));

			popup.add(new JMenuItem("Delete")).addActionListener(e -> {
				var paths = getSelectionPaths();
				if (paths != null) {
					for (TreePath path : paths) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (node.getParent() != null) {
							model.removeNodeFromParent(node);
						}
					}
					model.reload();
					save();
				}
			});
		}

		popup.add(new JMenuItem("New Folder...")).addActionListener(e -> {
			var name = GuiHelper.getStringDialog(BookmarkTree.this, "New bookmark folder", "Enter the name of the new folder", null);
			if (!Utility.isEmpty(name)) {
				addEntry(createBookmark(BookmarkType.FOLDER, name), selectedNode);
			}
		});

		popup.add(new JMenuItem("New separator"))
				.addActionListener(e -> addEntry(createBookmark(BookmarkType.SEPARATOR, ""), selectedNode));

		return popup;
	}

	private Bookmark createBookmark(BookmarkType type, String name) {
		var b = new Bookmark();
		b.setPath("");
		b.setName(name);
		b.setCreated(new Date());
		b.setBookmarkType(type);
		b.setSelectedNo(-1);
		b.setPluginNo(-1);
		b.setExtArguments("");
		b.setNotes("");
		return b;
	}

	/**
	 * @param bookmark the bookmark to show
	 */
	private void show(Object o) {
		if (o instanceof Bookmark) {
			new BookmarkPanel(mainPanel.getParent(), (Bookmark) o, consoleStream).showDialog();
		}
	}

	private void save(BookmarkList bl, File file) {
		try {
			var jaxbContext = JAXBContext.newInstance(BookmarkList.class);
			var marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			bl.setTimestamp(new Date());

			if (file.exists()) {
				Files.move(file.toPath(), new File(file.getAbsolutePath() + ".bak").toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			}
			marshaller.marshal(bl, file);
		} catch (JAXBException | IOException ex) {
			JOptionPane.showMessageDialog(this, "Can't write file\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void save() {
		var root = (DefaultMutableTreeNode) model.getRoot();
		var bl = new BookmarkList();
		bl.setName(bookmarkList.getName());
		exportModel(root, bl.getBookmarks());
		save(bl, file);
	}

	private void exportModel(DefaultMutableTreeNode parent, List<Bookmark> list) {
		list.clear();

		for (var i = 0; i < model.getChildCount(parent); i++) {
			var child = model.getChild(parent, i);
			Optional.ofNullable(child).filter(DefaultMutableTreeNode.class::isInstance)
					.map(DefaultMutableTreeNode.class::cast).map(DefaultMutableTreeNode::getUserObject)
					.filter(Bookmark.class::isInstance).map(Bookmark.class::cast).ifPresent(b -> {
						list.add(b);
						if (!((DefaultMutableTreeNode) child).isLeaf()) {
							exportModel((DefaultMutableTreeNode) child, b.getChilds());
						}
					});
		}
	}

	private void addBookmark(DefaultMutableTreeNode root, JMenuItem menu, Bookmark b, Icon icon) {
		root.add(new DefaultMutableTreeNode(b, false));
		var menuitem = new JMenuItem(b.getName(), icon);
		menuitem.addActionListener(e -> openBookmark(b));
		if (menu == null) {
			menuItems.add(menuitem);
		} else {
			menu.add(menuitem);
		}

		var button = new JButton(b.getName());
		button.addActionListener(e -> openBookmark(b));
		buttons.add(button);
	}

	private void openBookmark(Bookmark b) {
		var dp = mainPanel.getActiveDiskPanel();
		if (dp != null) {

			if (b.isZipped()) {
				// TODO
			}

			var f = new File(b.getPath());
			if (!f.exists() && f.getParentFile() != null && f.getParentFile().isFile()) {
				var pf = f.getParentFile();
				dp.doubleClickedLocalfile(pf, 0);

				if (dp.isZipFileLoaded()) {
					Integer n = dp.getDirEntries().filter(d -> f.getName().equals(d.getName())).findFirst().map(DirEntry::getNumber).orElse(null);
					if (n != null) {
						dp.doubleClickedRow(n.intValue() - 1);
					}
				}

			} else {
				dp.doubleClickedLocalfile(f, 0);
			}

			dp.setSelectedRow(b.getSelectedNo());

			if (!Utility.isEmpty(b.getNotes())) {
				var tp = new TextViewPanel(mainPanel);
				tp.setModal(false);
				tp.show(b.getNotes(), "Notes", b.getName(), Utility.MIMETYPE_TEXT);
			}

			if (b.getPluginNo() >= 0) {
				var ep = Setting.getExternalProgram(b.getPluginNo());
				if (ep != null && !Utility.isEmpty(ep.getCommand())) {
					ep.setExtArguments(b.getExtArguments());
					dp.doExternalProgram(ep);
				}
			}
		}
	}

	private void loadTree(DefaultMutableTreeNode root, JMenuItem menu, List<Bookmark> list) {
		for (var b : list) {
			switch (b.getBookmarkType()) {
			case DIRECTORY:
				addBookmark(root, menu, b, DIRECTORY_ICON);
				break;
			case DISKIMAGE:
				addBookmark(root, menu, b, DISKIMAGE_ICON);
				break;
			case SEPARATOR:
				root.add(new DefaultMutableTreeNode(b, false));
				if (menu == null) {
					menuItems.add(new JSeparator());
				} else {
					menu.add(new JSeparator());
				}
				break;
			case FOLDER:
				var submenu = new JMenu(b.getName());
				if (menu == null) {
					menuItems.add(submenu);
				} else {
					menu.add(submenu);
				}
				var dmtn = new DefaultMutableTreeNode(b, true);
				root.add(dmtn);
				loadTree(dmtn, submenu, b.getChilds());
				break;
			case ROOT:
				break;
			}
		}
	}
}

/**
 * TreeTransferHandler
 */
class TreeTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""	+ DefaultMutableTreeNode[].class.getName() + "\"";
	private DataFlavor nodesFlavor;
	private DataFlavor[] flavors = new DataFlavor[1];
	private DefaultMutableTreeNode[] nodesToRemove;
	private final MainPanel mainPanel;

	public TreeTransferHandler(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		try {
			nodesFlavor = new DataFlavor(MIME_TYPE);
			flavors[0] = nodesFlavor;
		} catch (ClassNotFoundException e) {
			GuiHelper.showErrorMessage(mainPanel.getParent(), "Drop failed", "ClassNotFound: " + e.getMessage());
		}
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		support.setShowDropLocation(true);
		if (!support.isDataFlavorSupported(nodesFlavor)) {
			return false;
		}
		// Do not allow a drop on the drag source selections.
		var dl = (JTree.DropLocation) support.getDropLocation();
		var tree = (JTree) support.getComponent();
		int dropRow = tree.getRowForPath(dl.getPath());
		var selRows = tree.getSelectionRows();

		for (int r : selRows) {
			if (r == dropRow) {
				return false;
			}
		}

		var target = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();

		var obj = target.getUserObject();
		if (!(obj instanceof BookmarkList)
				&& !Utility.isOneOf(((Bookmark) obj).getBookmarkType(), BookmarkType.ROOT, BookmarkType.FOLDER)) {
			// Can only drop to root or folder
			return false;
		}

		// Do not allow MOVE-action drops if a non-leaf node is
		// selected unless all of its children are also selected.
		if (support.getDropAction() == MOVE) {
			return haveCompleteNode(tree);
		}
		// Do not allow a non-leaf node to be copied to a level
		// which is less than its source level.
		var path = tree.getPathForRow(selRows[0]);
		var firstNode = (DefaultMutableTreeNode) path.getLastPathComponent();

		return firstNode.getChildCount() == 0 || target.getLevel() >= firstNode.getLevel();
	}

	private boolean haveCompleteNode(JTree tree) {
		var selRows = tree.getSelectionRows();
		var path = tree.getPathForRow(selRows[0]);
		var first = (DefaultMutableTreeNode) path.getLastPathComponent();
		var childCount = first.getChildCount();
		// first has children and no children are selected.
		if (childCount > 0 && selRows.length == 1) {
			return false;
		}
		// first may have children.
		for (int i = 1; i < selRows.length; i++) {
			path = tree.getPathForRow(selRows[i]);
			var next = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (first.isNodeChild(next) && childCount > selRows.length - 1) {
				// Found a child of first.
				// Not all children of first are selected.
				return false;
			}
		}
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent tree) {
		var paths = ((JTree) tree).getSelectionPaths();
		if (paths != null) {
			// Make up a node array of copies for transfer and
			// another for/of the nodes that will be removed in
			// exportDone after a successful drop.
			List<DefaultMutableTreeNode> copies = new ArrayList<>();
			List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
			var node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
			var copy = copy(node);
			copies.add(copy);
			toRemove.add(node);

			for (int i = 1; i < paths.length; i++) {
				var next = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
				// Do not allow higher level nodes to be added to list.
				if (next.getLevel() < node.getLevel()) {
					break;
				} else if (next.getLevel() > node.getLevel()) {
					// child node
					copy.add(copy(next));
					// node already contains child
				} else {
					// sibling
					copies.add(copy(next));
					toRemove.add(next);
				}
			}
			var nodes = copies.toArray(new DefaultMutableTreeNode[copies.size()]);
			nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
			return new NodesTransferable(nodes);
		}
		return null;
	}

	/** Defensive copy used in createTransferable. */
	private DefaultMutableTreeNode copy(TreeNode node) {
		return (DefaultMutableTreeNode) ((DefaultMutableTreeNode) node).clone();
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if ((action & MOVE) == MOVE) {
			var tree = (JTree) source;
			var model = (DefaultTreeModel) tree.getModel();
			// Remove nodes saved in nodesToRemove in createTransferable.
			for (var n : nodesToRemove) {
				model.removeNodeFromParent(n);
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		// Extract transfer data.
		DefaultMutableTreeNode[] nodes = null;
		try {
			nodes = (DefaultMutableTreeNode[]) support.getTransferable().getTransferData(nodesFlavor);
		} catch (UnsupportedFlavorException | IOException ex) {
			GuiHelper.showErrorMessage(mainPanel.getParent(), "Drop failed", ex.getMessage());
			return false;
		}
		// Get drop location info.
		var dl = (JTree.DropLocation) support.getDropLocation();
		int childIndex = dl.getChildIndex();
		var parent = (DefaultMutableTreeNode) dl.getPath().getLastPathComponent();
		var tree = (JTree) support.getComponent();
		// Configure for drop mode.
		int index = childIndex; // DropMode.INSERT
		if (childIndex == -1) { // DropMode.ON
			index = parent.getChildCount();
		}
		// Add data to model.
		var model = (DefaultTreeModel) tree.getModel();
		for (var i = 0; i < nodes.length; i++) {
			model.insertNodeInto(nodes[i], parent, index++);
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	/**
	 * NodesTransferable
	 */
	public class NodesTransferable implements Transferable {
		private final DefaultMutableTreeNode[] nodes;

		public NodesTransferable(DefaultMutableTreeNode[] nodes) {
			this.nodes = nodes;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return nodes;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return nodesFlavor.equals(flavor);
		}
	}
}
