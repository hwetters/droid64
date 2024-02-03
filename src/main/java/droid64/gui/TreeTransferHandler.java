package droid64.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import droid64.cfg.Bookmark;
import droid64.cfg.BookmarkList;
import droid64.cfg.BookmarkType;
import droid64.d64.Utility;

/**
 * TreeTransferHandler
 */
public class TreeTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
			+ DefaultMutableTreeNode[].class.getName() + "\"";
	private DataFlavor nodesFlavor;
	private DataFlavor[] flavors = new DataFlavor[1];
	private DefaultMutableTreeNode[] nodesToRemove;
	private final MainPanel mainPanel;

	/** 
	 * Constructor 
	 * @param mainPanel
	 * */
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
		final DefaultMutableTreeNode[] nodes;
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
